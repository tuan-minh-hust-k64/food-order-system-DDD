package com.food.ordering.system.payment.service.dataaccess.outbox.adaptor;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.dataaccess.outbox.exception.OrderOutboxNotFoundException;
import com.food.ordering.system.payment.service.dataaccess.outbox.mapper.OrderOutboxDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.outbox.repository.OrderOutboxMessageJpaRepository;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.repository.OrderOutboxRepository;
import com.food.ordering.system.valueobject.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderOutboxMessageRepositoryImpl implements OrderOutboxRepository {
    private final OrderOutboxMessageJpaRepository orderOutboxMessageJpaRepository;
    private final OrderOutboxDataAccessMapper orderOutboxDataAccessMapper;

    public OrderOutboxMessageRepositoryImpl(OrderOutboxMessageJpaRepository orderOutboxMessageJpaRepository,
                                            OrderOutboxDataAccessMapper orderOutboxDataAccessMapper) {
        this.orderOutboxMessageJpaRepository = orderOutboxMessageJpaRepository;
        this.orderOutboxDataAccessMapper = orderOutboxDataAccessMapper;
    }

    @Override
    public OrderOutboxMessage save(OrderOutboxMessage orderOutboxMessage) {
        return orderOutboxDataAccessMapper.orderOutboxEntityToOrderOutboxMessage(
                orderOutboxMessageJpaRepository.save(
                        orderOutboxDataAccessMapper.orderOutboxMessageToOrderOutboxEntity(orderOutboxMessage)
                )
        );
    }

    @Override
    public Optional<List<OrderOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus status) {
        return Optional.of(orderOutboxMessageJpaRepository.findByTypeAndOutboxStatus(type, status)
                .orElseThrow(() -> new OrderOutboxNotFoundException("Approval outbox object " +
                        "cannot be found for saga type " + type))
                .stream()
                .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage)
                .collect(Collectors.toList()))
        ;
    }

    @Override
    public Optional<OrderOutboxMessage> findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(String type, UUID sagaId, PaymentStatus paymentStatus, OutboxStatus outboxStatus) {
        return orderOutboxMessageJpaRepository.findByTypeAndSagaIdAndPaymentStatusAndOutboxStatus(type, sagaId, paymentStatus, outboxStatus)
                .map(orderOutboxDataAccessMapper::orderOutboxEntityToOrderOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus status) {
        orderOutboxMessageJpaRepository.deleteByTypeAndOutboxStatus(type, status);
    }
}
