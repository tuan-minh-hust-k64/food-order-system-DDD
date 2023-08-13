package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.order.SagaConstant;
import com.food.ordering.system.valueobject.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class PaymentOutboxHelper {
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentOutboxHelper(PaymentOutboxRepository paymentOutboxRepository, ObjectMapper objectMapper) {
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.objectMapper = objectMapper;
    }
    @Transactional(readOnly = true)
    public Optional<List<OrderPaymentOutboxMessage>> getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
            OutboxStatus outboxStatus, SagaStatus... sagaStatus
    ) {
        return paymentOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus, sagaStatus);
    }
    @Transactional(readOnly = true)
    public Optional<OrderPaymentOutboxMessage> getPaymentOutboxMessageBySagaIdAndSagaStatus(
            UUID sagaId, SagaStatus... sagaStatuses
    ) {
        return paymentOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, sagaId, sagaStatuses);
    }
    @Transactional
    public void save(OrderPaymentOutboxMessage orderPaymentOutboxMessage) {
        OrderPaymentOutboxMessage res = paymentOutboxRepository.save(orderPaymentOutboxMessage);
        if(res == null) {
            log.error("Could not save OrderPaymentOutboxMessage with outbox id: {}", orderPaymentOutboxMessage.getId());
            throw new OrderDomainException("Could not save OrderPaymentOutboxMessage with outbox id: {}" + orderPaymentOutboxMessage.getId());
        }
        log.info("Saved OrderPaymentOutboxMessage with outbox id: {}", orderPaymentOutboxMessage.getId());
    }
    @Transactional
    public void deleteByTypeAndOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        paymentOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }
    @Transactional
    public void savePaymentOutboxMessage(
            OrderPaymentEventPayload orderPaymentEventPayload,
            OrderStatus orderStatus,
            SagaStatus sagaStatus,
            OutboxStatus outboxStatus,
            UUID sagaId
            ) {
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = OrderPaymentOutboxMessage.builder()
                .createdAt(orderPaymentEventPayload.getCreatedAt())
                .id(UUID.randomUUID())
                .type(SagaConstant.ORDER_SAGA_NAME)
                .payload(createPayloadPaymentOutboxMessage(orderPaymentEventPayload))
                .outboxStatus(outboxStatus)
                .sagaId(sagaId)
                .sagaStatus(sagaStatus)
                .orderStatus(orderStatus)
                .build();
        save(orderPaymentOutboxMessage);
    }

    private String createPayloadPaymentOutboxMessage(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderPaymentEventPayload object for order id: {}",
                    orderPaymentEventPayload.getOrderId());
            throw new OrderDomainException("Could not create OrderPaymentEventPayload object for order id: " +
                    orderPaymentEventPayload.getOrderId(), e);
        }
    }
}
