package com.food.ordering.system.service.messaging.publish.kafka;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderKafkaMessageHelper orderKafkaMessageHelper;

    public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                         OrderServiceConfigData orderServiceConfigData,
                                         KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer, OrderKafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().toString();
        RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper.orderPaidEventToRestaurantApprovalRequestAvroModel(domainEvent);
        try {
            kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(), orderId,
                    restaurantApprovalRequestAvroModel,
                    orderKafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getRestaurantApprovalResponseTopicName(),
                            restaurantApprovalRequestAvroModel,
                            orderId, "RestaurantApprovalRequestAvroModel"));
            log.info("RestaurantApprovalRequestAvroModel sent to kafka for order id: {}", orderId);
        } catch (Exception e) {
            log.error("Error while sending restaurantApprovalRequestAvroModel message" + "to kafka with order id: {}, error {}", orderId, e.getMessage());
        }

    }
}