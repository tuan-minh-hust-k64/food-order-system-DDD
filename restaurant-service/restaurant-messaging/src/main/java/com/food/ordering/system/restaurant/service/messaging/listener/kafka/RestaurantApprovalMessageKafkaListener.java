package com.food.ordering.system.restaurant.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@Slf4j
public class RestaurantApprovalMessageKafkaListener implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {
    private final RestaurantApprovalRequestListener restaurantApprovalRequestListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    public RestaurantApprovalMessageKafkaListener(RestaurantApprovalRequestListener restaurantApprovalRequestListener, RestaurantMessagingDataMapper restaurantMessagingDataMapper) {
        this.restaurantApprovalRequestListener = restaurantApprovalRequestListener;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(
            @Payload List<RestaurantApprovalRequestAvroModel> message,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of orders approval requests received with keys {}, partitions {} and offsets {}" +
                        ", sending for restaurant approval",
                message.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());
        message.forEach(restaurantApprovalRequestAvroModel -> {
            log.info("Processing order approval for order id: {}", restaurantApprovalRequestAvroModel.getOrderId());
            restaurantApprovalRequestListener.approveOrder(restaurantMessagingDataMapper.orderApprovalRequestToOrderApproval(restaurantApprovalRequestAvroModel));
        });
    }
}
