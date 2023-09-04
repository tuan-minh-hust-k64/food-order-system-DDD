package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.event.payload.RestaurantOrderEventPayload;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.order.service.domain.RestaurantResponseMessageListenerImpl;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import debezium.restaurant.order_outbox.Envelope;
import debezium.restaurant.order_outbox.Value;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<Envelope> {
    private final RestaurantResponseMessageListenerImpl restaurantResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantApprovalResponseKafkaListener(RestaurantResponseMessageListenerImpl restaurantResponseMessageListener,
                                                   OrderMessagingDataMapper orderMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
        this.restaurantResponseMessageListener = restaurantResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
        topics = "${order-service.restaurant-approval-response-topic-name}"
    )
    public void receive(@Payload List<Envelope> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant responses received!",
                messages.stream().filter(message -> message.getBefore() == null &&
                        DebeziumOp.CREATE.getValue().equals(message.getOp())).toList().size());
        messages.forEach(avroModel -> {
            if(avroModel.getBefore() == null && DebeziumOp.CREATE.getValue().equals(avroModel.getOp())) {
                Value restaurantApprovalResponseAvroModel = avroModel.getAfter();
                RestaurantOrderEventPayload restaurantOrderEventPayload = kafkaMessageHelper.getEventPayload(
                        restaurantApprovalResponseAvroModel.getPayload(), RestaurantOrderEventPayload.class
                );

                try {
                    if(restaurantOrderEventPayload.getOrderApprovalStatus().equals(OrderApprovalStatus.APPROVED.name())) {
                        log.info("Processing approved for order id: {}", restaurantOrderEventPayload.getOrderId());
                        restaurantResponseMessageListener.orderApporoved(orderMessagingDataMapper
                                .restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(restaurantOrderEventPayload, restaurantApprovalResponseAvroModel));
                    } else if(restaurantOrderEventPayload.getOrderApprovalStatus().equals(OrderApprovalStatus.REJECTED.name())) {
                        log.info("Processing reject for order id: {}, with failure message: {}",
                                restaurantOrderEventPayload.getOrderId(),
                                String.join(",", restaurantOrderEventPayload.getFailureMessages())
                        );
                        restaurantResponseMessageListener.orderRejected(orderMessagingDataMapper
                                .restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(restaurantOrderEventPayload, restaurantApprovalResponseAvroModel));
                    }
                } catch (OptimisticLockingFailureException e) {
                    //NO-OP for optimistic lock. This means another thread finished the work, do not throw error to prevent reading the data from kafka again!
                    log.error("Caught optimistic locking exception in PaymentResponseKafkaListener for order id: {}",
                            restaurantOrderEventPayload.getOrderId());
                } catch (OrderNotFoundException e) {
                    //NO-OP for OrderNotFoundException
                    log.error("No order found for order id: {}", restaurantOrderEventPayload.getOrderId());
                } catch (DataAccessException e) {
                    SQLException sqlException = (SQLException) e.getRootCause();
                    if (sqlException != null && sqlException.getSQLState() != null &&
                            PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                        //NO-OP for unique constraint exception
                        log.error("Caught unique constraint exception with sql state: {} " +
                                        "in PaymentResponseKafkaListener for order id: {}",
                                sqlException.getSQLState(), restaurantOrderEventPayload.getOrderId());
                    }
                }
            }

        });
    }
}
