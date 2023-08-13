package com.food.ordering.system.order.service.messaging.publish.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.KafkaMessageHelper;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
@Slf4j
@Component
public class OrderPaymentEventKafkaPublisher implements PaymentRequestMessagePublisher {
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final ObjectMapper objectMapper;

    public OrderPaymentEventKafkaPublisher(KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
                                           OrderServiceConfigData orderServiceConfigData,
                                           KafkaMessageHelper kafkaMessageHelper,
                                           OrderMessagingDataMapper orderMessagingDataMapper, ObjectMapper objectMapper) {
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage, BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        OrderPaymentEventPayload orderPaymentEventPayload = getOrderPaymentEvenPayload(orderPaymentOutboxMessage.getPayload());
        String sagaId = orderPaymentOutboxMessage.getSagaId().toString();
        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper.orderPaymentEventPayloadToPaymentRequestAvroModel(orderPaymentEventPayload, sagaId);
            kafkaProducer.send(
                    orderServiceConfigData.getPaymentRequestTopicName(),
                    sagaId, paymentRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            paymentRequestAvroModel,
                            sagaId,
                            "PaymentRequestAvroModel",
                            orderPaymentOutboxMessage,
                            outboxCallback
                    ));
            log.info("OrderPaymentEventPayload sent to Kafka for order id: {} and saga id: {}",
                    orderPaymentEventPayload.getOrderId(), sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload" +
                            " to kafka with order id: {} and saga id: {}, error: {}",
                    orderPaymentEventPayload.getOrderId(), sagaId, e.getMessage());
        }
    }

    private OrderPaymentEventPayload getOrderPaymentEvenPayload(String payload) {
        try {
            return objectMapper.readValue(payload, OrderPaymentEventPayload.class);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Could not read value OrderPaymentEventPayload" + e);
        }
    }
}
