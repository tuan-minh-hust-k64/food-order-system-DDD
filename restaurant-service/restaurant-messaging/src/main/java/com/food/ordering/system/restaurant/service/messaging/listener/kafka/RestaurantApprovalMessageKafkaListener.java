package com.food.ordering.system.restaurant.service.messaging.listener.kafka;

import com.food.ordering.system.event.payload.OrderApprovalEventPayload;
import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.messaging.DebeziumOp;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantApplicationServiceException;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import debezium.order.restaurant_approval_outbox.Envelope;
import debezium.order.restaurant_approval_outbox.Value;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
@Component
@Slf4j
public class RestaurantApprovalMessageKafkaListener implements KafkaConsumer<Envelope> {
    private final RestaurantApprovalRequestListener restaurantApprovalRequestListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantApprovalMessageKafkaListener(RestaurantApprovalRequestListener restaurantApprovalRequestListener, RestaurantMessagingDataMapper restaurantMessagingDataMapper, KafkaMessageHelper kafkaMessageHelper) {
        this.restaurantApprovalRequestListener = restaurantApprovalRequestListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(
            @Payload List<Envelope> messages,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant responses received!",
                messages.stream().filter(message -> message.getBefore() == null &&
                        DebeziumOp.CREATE.getValue().equals(message.getOp())).toList().size());
        messages.forEach(avroModel -> {
            if(avroModel.getBefore() == null && avroModel.getOp().equals(DebeziumOp.CREATE.getValue())) {
                Value restaurantApprovalRequestAvroModel = avroModel.getAfter();
                OrderApprovalEventPayload orderApprovalEventPayload = kafkaMessageHelper.getEventPayload(
                        restaurantApprovalRequestAvroModel.getPayload(),
                        OrderApprovalEventPayload.class
                );

                try {
                    log.info("Processing order approval for order id: {}", orderApprovalEventPayload.getOrderId());
                    restaurantApprovalRequestListener.approveOrder(restaurantMessagingDataMapper.orderApprovalRequestToOrderApproval(orderApprovalEventPayload, restaurantApprovalRequestAvroModel));
                } catch (DataAccessException e) {
                    SQLException sqlException = (SQLException) e.getRootCause();
                    if (sqlException != null && sqlException.getSQLState() != null &&
                            PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                        //NO-OP for unique constraint exception
                        log.error("Caught unique constraint exception with sql state: {} " +
                                        "in RestaurantApprovalRequestKafkaListener for order id: {}",
                                sqlException.getSQLState(), orderApprovalEventPayload.getOrderId());
                    } else {
                        throw new RestaurantApplicationServiceException("Throwing DataAccessException in" +
                                " RestaurantApprovalRequestKafkaListener: " + e.getMessage(), e);
                    }
                } catch (RestaurantNotFoundException e) {
                    //NO-OP for RestaurantNotFoundException
                    log.error("No restaurant found for restaurant id: {}, and order id: {}",
                            orderApprovalEventPayload.getRestaurantId(),
                            orderApprovalEventPayload.getOrderId());
                }
            }

        });
    }
}
