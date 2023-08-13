package com.food.ordering.system.kafka.producer;

import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaMessageHelper {
    public <T, U> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(
            String responseTopicName,
            T requestAvroModel,
            String orderId,
            String requestAvroModelName,
            U outboxMessage,
            BiConsumer<U, OutboxStatus> callback) {
        return new ListenableFutureCallback<SendResult<String, T>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending paymentRequestAvroModel" + requestAvroModelName + "message {} to topic {}", requestAvroModel.toString(), responseTopicName, ex);
                callback.accept(outboxMessage, OutboxStatus.FAILED);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Receive success response from kafka for order id: {}" + " Topic: {} Partition: {} Offset: {} Timestamp: {}",
                        orderId,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp()
                );
                callback.accept(outboxMessage, OutboxStatus.COMPLETED);
            }
        };
    }
}
