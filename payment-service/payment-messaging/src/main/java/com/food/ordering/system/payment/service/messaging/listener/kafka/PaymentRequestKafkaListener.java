package com.food.ordering.system.payment.service.messaging.listener.kafka;

import com.food.ordering.system.event.payload.PaymentOrderEventPayload;
import com.food.ordering.system.kafka.consumer.KafkaSingleItemConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;
import debezium.order.payment_outbox.Envelope;
import debezium.order.payment_outbox.Value;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component
public class PaymentRequestKafkaListener implements KafkaSingleItemConsumer<Envelope> {

    private final PaymentRequestMessageListener paymentRequestMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    public PaymentRequestKafkaListener(PaymentRequestMessageListener paymentRequestMessageListener,
                                       PaymentMessagingDataMapper paymentMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
        this.paymentRequestMessageListener = paymentRequestMessageListener;
        this.paymentMessagingDataMapper = paymentMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.payment-consumer-group-id}",
                topics = "${payment-service.payment-request-topic-name}")
    public void receive(@Payload Envelope message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partitions,
                        @Header(KafkaHeaders.OFFSET) Long offsets) {
        log.info("1 number of payment requests received with keys:{}, partitions:{} and offsets: {}",
                keys,
                partitions.toString(),
                offsets.toString());
        if (message.getBefore() == null && message.getOp().equals(DebeziumOp.CREATE.getValue())) {
            Value paymentRequestAvroModel = message.getAfter();
            PaymentOrderEventPayload paymentOrderEventPayload = kafkaMessageHelper.getEventPayload(
                    paymentRequestAvroModel.getPayload(), PaymentOrderEventPayload.class
            );

                try {
                    if (PaymentOrderStatus.PENDING.name().equals(paymentOrderEventPayload.getPaymentOrderStatus())) {
                        log.info("Processing payment for order id: {}", paymentOrderEventPayload.getOrderId());
                        paymentRequestMessageListener.completePayment(paymentMessagingDataMapper
                                .paymentRequestAvroModelToPaymentRequest(paymentOrderEventPayload, paymentRequestAvroModel));
                    } else if(PaymentOrderStatus.CANCELLED.name().equals(paymentOrderEventPayload.getPaymentOrderStatus())) {
                        log.info("Cancelling payment for order id: {}", paymentOrderEventPayload.getOrderId());
                        paymentRequestMessageListener.cancelPayment(paymentMessagingDataMapper
                                .paymentRequestAvroModelToPaymentRequest(paymentOrderEventPayload, paymentRequestAvroModel));
                    }
                } catch (DataAccessException e) {
                    SQLException sqlException = (SQLException) e.getRootCause();
                    if (sqlException != null && sqlException.getSQLState() != null &&
                            PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                        //NO-OP for unique constraint exception
                        log.error("Caught unique constraint exception with sql state: {} " +
                                        "in PaymentRequestKafkaListener for order id: {}",
                                sqlException.getSQLState(), paymentOrderEventPayload.getOrderId());
                    } else {
                        throw new PaymentApplicationServiceException("Throwing DataAccessException in" +
                                " PaymentRequestKafkaListener: " + e.getMessage(), e);
                    }
                } catch (PaymentNotFoundException e) {
                    //NO-OP for PaymentNotFoundException
                    log.error("No payment found for order id: {}", paymentOrderEventPayload.getOrderId());
                }

        }


    }
}
