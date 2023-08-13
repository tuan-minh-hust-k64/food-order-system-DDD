package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.RestaurantApprovalOutboxHelp;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.valueobject.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse> {
    private final OrderDomainService orderDomainService;
    private final OrderSagaHelp orderSagaHelp;
    private final RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderApprovalSaga(OrderDomainService orderDomainService,
                             OrderSagaHelp orderSagaHelp,
                             RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp,
                             PaymentOutboxHelper paymentOutboxHelper, OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelp = orderSagaHelp;
        this.restaurantApprovalOutboxHelp = restaurantApprovalOutboxHelp;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    @Transactional
    public void process(RestaurantApprovalResponse data) {
        log.info("Order approval success with id: {}", data.getOrderId());
        Order order = orderSagaHelp.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelp.saveOrder(order);

        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageOptional = restaurantApprovalOutboxHelp.getOrderApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                SagaStatus.PROCESSING
        );
        if(orderApprovalOutboxMessageOptional.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", data.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageOptional.get();
        SagaStatus sagaStatus = orderSagaHelp.orderStatusToSagaStatus(order.getOrderStatus());
        restaurantApprovalOutboxHelp.save(getOrderApprovalOutboxMessageUpdate(orderApprovalOutboxMessage, order.getOrderStatus(), sagaStatus));
        paymentOutboxHelper.save(getOrderPaymentOutboxMessageUpdate(data.getSagaId(), order.getOrderStatus(), sagaStatus));
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessageUpdate(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderPaymentOutboxMessage> outboxMessageOptional = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(sagaId),
                SagaStatus.PROCESSING
                );
        if(outboxMessageOptional.isEmpty()) {
            throw new OrderDomainException("Payment outbox message cannot be found in " +
                    SagaStatus.PROCESSING.name() + " state");
        }
        OrderPaymentOutboxMessage orderPaymentOutboxMessage = outboxMessageOptional.get();
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        return orderPaymentOutboxMessage;
    }

    private OrderApprovalOutboxMessage getOrderApprovalOutboxMessageUpdate(OrderApprovalOutboxMessage orderApprovalOutboxMessage,
                                                                           OrderStatus orderStatus, SagaStatus sagaStatus) {
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }

    @Override
    public void rollback(RestaurantApprovalResponse data) {
        log.info("Cancelled order with id: {}", data.getOrderId());
        Order order = orderSagaHelp.findOrder(data.getOrderId());
        OrderCancelEvent orderCancelEvent = orderDomainService.cancelOrderPayment(
                order,
                data.getFailureMessages());
        orderSagaHelp.saveOrder(order);

        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageOptional = restaurantApprovalOutboxHelp.getOrderApprovalOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                SagaStatus.PROCESSING
        );
        if(orderApprovalOutboxMessageOptional.isEmpty()) {
            log.info("An outbox message with saga id: {} is already processed!", data.getSagaId());
            return;
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageOptional.get();
        SagaStatus sagaStatus = orderSagaHelp.orderStatusToSagaStatus(orderCancelEvent.getOrder().getOrderStatus());
        restaurantApprovalOutboxHelp.save(getOrderApprovalOutboxMessageUpdate(orderApprovalOutboxMessage,
                orderCancelEvent.getOrder().getOrderStatus(), sagaStatus));
        paymentOutboxHelper.savePaymentOutboxMessage(
                orderDataMapper.orderCancelEventToPaymentOutboxMessagePayload(orderCancelEvent),
                orderCancelEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(data.getSagaId())
                );
        log.info("Order with id: {} is cancelling", orderCancelEvent.getOrder().getId().getValue());

    }
}
