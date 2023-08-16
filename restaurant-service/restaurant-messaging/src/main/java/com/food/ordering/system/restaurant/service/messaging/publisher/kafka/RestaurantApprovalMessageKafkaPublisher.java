package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantDataConfig;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publish.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
@Slf4j
@Component
public class RestaurantApprovalMessageKafkaPublisher implements RestaurantApprovalResponseMessagePublisher {
    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
    private final RestaurantDataConfig restaurantDataConfig;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;

    public RestaurantApprovalMessageKafkaPublisher(KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer,
                                                   RestaurantDataConfig restaurantDataConfig,
                                                   RestaurantMessagingDataMapper restaurantMessagingDataMapper,
                                                   KafkaMessageHelper kafkaMessageHelper) {
        this.kafkaProducer = kafkaProducer;
        this.restaurantDataConfig = restaurantDataConfig;
        this.restaurantMessagingDataMapper = restaurantMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(OrderOutboxMessage orderOutboxMessage, BiConsumer<OrderOutboxMessage, OutboxStatus> outboxCallback) {
        String sagaId = orderOutboxMessage.getSagaId().toString();
        OrderEventPayload orderEventPayload = kafkaMessageHelper.getEventPayload(
                orderOutboxMessage.getPayload(),
                OrderEventPayload.class
        );
        try {
            RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel = restaurantMessagingDataMapper.
                    orderEventPayloadToRestaurantApprovalResponseAvroModel(orderEventPayload, sagaId);
            kafkaProducer.send(
                    restaurantDataConfig.getRestaurantApprovalResponseTopicName(),
                    sagaId,
                    restaurantApprovalResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            restaurantDataConfig.getRestaurantApprovalResponseTopicName(),
                            restaurantApprovalResponseAvroModel,
                            orderEventPayload.getOrderId(),
                            "RestaurantApprovalResponseAvroModel",
                            orderOutboxMessage,
                            outboxCallback
                    )
            );
            log.info("RestaurantApprovalResponseAvroModel sent to kafka for order id: {} and saga id: {}",
                    restaurantApprovalResponseAvroModel.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message" +
                            " to kafka with order id: {} and saga id: {}, error: {}",
                    orderEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }
}
