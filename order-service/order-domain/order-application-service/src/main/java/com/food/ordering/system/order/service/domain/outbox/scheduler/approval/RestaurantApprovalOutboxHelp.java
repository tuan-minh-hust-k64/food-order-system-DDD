package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.ApprovalOutboxRepository;
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
public class RestaurantApprovalOutboxHelp {
    private final ApprovalOutboxRepository approvalOutboxRepository;
    private final ObjectMapper objectMapper;

    public RestaurantApprovalOutboxHelp(ApprovalOutboxRepository approvalOutboxRepository,
                                        RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher, ObjectMapper objectMapper) {
        this.approvalOutboxRepository = approvalOutboxRepository;
        this.objectMapper = objectMapper;
    }
    @Transactional(readOnly = true)
    public Optional<List<OrderApprovalOutboxMessage>> getOrderApprovalOutboxMessageByOutboxStatusAndSagaStatus(
            OutboxStatus outboxStatus, SagaStatus... sagaStatuses
    ) {
        return approvalOutboxRepository.findByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }
    @Transactional(readOnly = true)
    public Optional<OrderApprovalOutboxMessage> getOrderApprovalOutboxMessageBySagaIdAndSagaStatus(UUID sagaId, SagaStatus... sagaStatuses) {
        return approvalOutboxRepository.findByTypeAndSagaIdAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, sagaId, sagaStatuses);
    }
    @Transactional
    public void save(OrderApprovalOutboxMessage orderApprovalOutboxMessage) {
        OrderApprovalOutboxMessage res = approvalOutboxRepository.save(orderApprovalOutboxMessage);
        if(res == null) {
            log.error("Could not save OrderApprovalOutboxMessage with outbox id: {}", orderApprovalOutboxMessage.getId());
            throw new OrderDomainException("Could not save OrderApprovalOutboxMessage with outbox id: {}" + orderApprovalOutboxMessage.getId());
        }
        log.info("Saved OrderPaymentOutboxMessage with outbox id: {}", orderApprovalOutboxMessage.getId());
    }
    @Transactional
    public void deleteByTypeAndOutboxStatusAndSagaStatus(OutboxStatus outboxStatus, SagaStatus... sagaStatuses) {
        approvalOutboxRepository.deleteByTypeAndOutboxStatusAndSagaStatus(SagaConstant.ORDER_SAGA_NAME, outboxStatus, sagaStatuses);
    }
    @Transactional
    public void saveApprovalOutboxMessage(OrderApprovalEventPayload orderApprovalEventPayload,
                                          OrderStatus orderStatus,
                                          SagaStatus sagaStatus,
                                          OutboxStatus outboxStatus,
                                          UUID sagaId) {
        save(OrderApprovalOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(orderApprovalEventPayload.getCreatedAt())
                .type(SagaConstant.ORDER_SAGA_NAME)
                .payload(createPayload(orderApprovalEventPayload))
                .orderStatus(orderStatus)
                .sagaStatus(sagaStatus)
                .outboxStatus(outboxStatus)
                .build());
    }
    private String createPayload(OrderApprovalEventPayload orderApprovalEventPayload) {
        try {
            return objectMapper.writeValueAsString(orderApprovalEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create OrderApprovalEventPayload for order id: {}",
                    orderApprovalEventPayload.getOrderId(), e);
            throw new OrderDomainException("Could not create OrderApprovalEventPayload for order id: " +
                    orderApprovalEventPayload.getOrderId(), e);
        }
    }
}
