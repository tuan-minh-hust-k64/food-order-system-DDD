package com.food.ordering.system.order.service.messaging.publish.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
@Slf4j
@Component
public class OrderApprovalEventKafkaPublisher implements RestaurantApprovalRequestMessagePublisher {
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final ObjectMapper objectMapper;

    public OrderApprovalEventKafkaPublisher(KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
                                            OrderMessagingDataMapper orderMessagingDataMapper,
                                            KafkaMessageHelper kafkaMessageHelper,
                                            OrderServiceConfigData orderServiceConfigData, ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderApprovalOutboxMessage orderApprovalOutboxMessage, BiConsumer<OrderApprovalOutboxMessage, OutboxStatus> outboxCallback) {
        OrderApprovalEventPayload orderApprovalEventPayload = getRestaurantApprovalRequestAvroModel(orderApprovalOutboxMessage.getPayload());
        String sagaId = orderApprovalOutboxMessage.getSagaId().toString();

        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper.
                    orderApprovalEventPayloadToRestaurantApprovalRequestAvroModel(orderApprovalEventPayload, sagaId);
            kafkaProducer.send(
                    orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    sagaId,
                    restaurantApprovalRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                            restaurantApprovalRequestAvroModel,
                            sagaId,
                            "RestaurantApprovalAvroModel",
                            orderApprovalOutboxMessage,
                            outboxCallback
                    )
            );
            log.info("OrderApprovalEventPayload sent to kafka for order id: {} and saga id: {}",
                    restaurantApprovalRequestAvroModel.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderApprovalEventPayload to kafka for order id: {} and saga id: {}," +
                    " error: {}", orderApprovalEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }

    private OrderApprovalEventPayload getRestaurantApprovalRequestAvroModel(String payload) {
        try {
            return objectMapper.readValue(payload, OrderApprovalEventPayload.class);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Could not read value OrderPaymentEventPayload" + e);
        }
    }
}
