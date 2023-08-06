package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.RestaurantResponseMessageListenerImpl;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {
    private final RestaurantResponseMessageListenerImpl restaurantResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantApprovalResponseKafkaListener(RestaurantResponseMessageListenerImpl restaurantResponseMessageListener,
                                                   OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantResponseMessageListener = restaurantResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
        topics = "${order-service.restaurant-approval-response-topic-name}"
    )
    public void receive(@Payload List<RestaurantApprovalResponseAvroModel> message,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of restaurant approval response received with keys: {}, partition: {}, offset: {}",
                    message.size(),
                    keys.toString(),
                    partitions.toString(),
                    offsets.toString()
                );
        message.forEach(restaurantApprovalResponseAvroModel -> {
            if(restaurantApprovalResponseAvroModel.getOrderApprovalStatus() == OrderApprovalStatus.APPROVED) {
                log.info("Processing approved for order id: {}", restaurantApprovalResponseAvroModel.getOrderId());
                restaurantResponseMessageListener.orderApporoved(orderMessagingDataMapper
                        .restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(restaurantApprovalResponseAvroModel));
            } else if(restaurantApprovalResponseAvroModel.getOrderApprovalStatus() == OrderApprovalStatus.REJECTED) {
                log.info("Processing reject for order id: {}, with failure message: {}",
                        restaurantApprovalResponseAvroModel.getOrderId(),
                        String.join(",", restaurantApprovalResponseAvroModel.getFailureMessages())
                );
                restaurantResponseMessageListener.orderRejected(orderMessagingDataMapper
                        .restaurantApprovalRequestAvroModelToRestaurantApprovalRequest((restaurantApprovalResponseAvroModel)));
            }
        });
    }
}
