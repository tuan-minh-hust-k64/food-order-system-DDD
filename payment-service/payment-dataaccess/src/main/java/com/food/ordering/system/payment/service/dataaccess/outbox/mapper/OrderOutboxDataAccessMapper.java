package com.food.ordering.system.payment.service.dataaccess.outbox.mapper;

import com.food.ordering.system.payment.service.dataaccess.outbox.entity.OrderOutboxEntity;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxDataAccessMapper {
    public OrderOutboxEntity orderOutboxMessageToOrderOutboxEntity(OrderOutboxMessage orderOutboxMessage) {
        return OrderOutboxEntity.builder()
                .id(orderOutboxMessage.getId())
                .outboxStatus(orderOutboxMessage.getOutboxStatus())
                .type(orderOutboxMessage.getType())
                .createdAt(orderOutboxMessage.getCreatedAt())
                .payload(orderOutboxMessage.getPayload())
                .paymentStatus(orderOutboxMessage.getPaymentStatus())
                .sagaId(orderOutboxMessage.getSagaId())
                .version(orderOutboxMessage.getVersion())
                .build();
    }
    public OrderOutboxMessage orderOutboxEntityToOrderOutboxMessage(OrderOutboxEntity orderOutboxEntity) {
        return OrderOutboxMessage.builder()
                .outboxStatus(orderOutboxEntity.getOutboxStatus())
                .createdAt(orderOutboxEntity.getCreatedAt())
                .id(orderOutboxEntity.getId())
                .payload(orderOutboxEntity.getPayload())
                .paymentStatus(orderOutboxEntity.getPaymentStatus())
                .sagaId(orderOutboxEntity.getSagaId())
                .type(orderOutboxEntity.getType())
                .version(orderOutboxEntity.getVersion())
                .build();
    }
}
