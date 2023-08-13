package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.RestaurantApprovalOutboxHelp;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.OrderStatus;
import com.food.ordering.system.valueobject.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {
    private final OrderDomainService orderDomainService;
    private final OrderSagaHelp orderSagaHelp;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp;
    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    public OrderPaymentSaga(OrderDomainService orderDomainService,
                            OrderSagaHelp orderSagaHelp,
                            PaymentOutboxHelper paymentOutboxHelper,
                            RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp, OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelp = orderSagaHelp;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.restaurantApprovalOutboxHelp = restaurantApprovalOutboxHelp;
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void process(PaymentResponse data) {
        log.info("Complete payment for order with id: {}", data.getOrderId());
        Optional<OrderPaymentOutboxMessage> outboxMessageOptional = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                UUID.fromString(data.getSagaId()),
                SagaStatus.STARTED
        );
        if(outboxMessageOptional.isEmpty()) {
            log.info("Order with id: {} processed!", data.getOrderId());
            return;
        }
        Order order = orderSagaHelp.findOrder(data.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);
        orderSagaHelp.saveOrder(order);
        SagaStatus sagaStatus = orderSagaHelp.orderStatusToSagaStatus(orderPaidEvent.getOrder().getOrderStatus());

        OrderPaymentOutboxMessage outboxMessage = outboxMessageOptional.get();
        paymentOutboxHelper.save(getOrderPaymentOutboxMessageUpdate(outboxMessage,
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus));
        restaurantApprovalOutboxHelp.saveApprovalOutboxMessage(orderDataMapper.orderPaidEventToOrderApprovalEventPayload(orderPaidEvent),
                orderPaidEvent.getOrder().getOrderStatus(),
                sagaStatus,
                OutboxStatus.STARTED,
                UUID.fromString(data.getSagaId())
                );
        log.info("Order with id: {} is paid", orderPaidEvent.getOrder().getId().getValue());

    }

    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessageUpdate(OrderPaymentOutboxMessage outboxMessage,
                                                                         OrderStatus orderStatus, SagaStatus sagaStatus) {
        outboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        outboxMessage.setOrderStatus(orderStatus);
        outboxMessage.setSagaStatus(sagaStatus);
        return outboxMessage;
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse paymentResponse) {
        Optional<OrderPaymentOutboxMessage> orderPaymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(paymentResponse.getSagaId()),
                        getCurrentSagaStatus(paymentResponse.getPaymentStatus()));

        if (orderPaymentOutboxMessageResponse.isEmpty()) {
            log.info("An outbox message with saga id: {} is already roll backed!", paymentResponse.getSagaId());
            return;
        }

        OrderPaymentOutboxMessage orderPaymentOutboxMessage = orderPaymentOutboxMessageResponse.get();

        Order order = rollbackPaymentForOrder(paymentResponse);

        SagaStatus sagaStatus = orderSagaHelp.orderStatusToSagaStatus(order.getOrderStatus());

        paymentOutboxHelper.save(getUpdatedPaymentOutboxMessage(orderPaymentOutboxMessage,
                order.getOrderStatus(), sagaStatus));

        if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
            restaurantApprovalOutboxHelp.save(getUpdatedApprovalOutboxMessage(paymentResponse.getSagaId(),
                    order.getOrderStatus(), sagaStatus));
        }

        log.info("Order with id: {} is cancelled", order.getId().getValue());
    }
    private SagaStatus[] getCurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[] { SagaStatus.STARTED };
            case CANCELLED -> new SagaStatus[] { SagaStatus.PROCESSING };
            case FAILED -> new SagaStatus[] { SagaStatus.STARTED, SagaStatus.PROCESSING };
        };
    }
    private Order rollbackPaymentForOrder(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderRepository.save(order);
        return order;
    }
    private Order findOrder(String orderId) {
        Optional<Order> orderResponse = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
        if (orderResponse.isEmpty()) {
            log.error("Order with id: {} could not be found!", orderId);
            throw new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        }
        return orderResponse.get();
    }
    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage(OrderPaymentOutboxMessage
                                                                             orderPaymentOutboxMessage,
                                                                     OrderStatus
                                                                             orderStatus,
                                                                     SagaStatus
                                                                             sagaStatus) {
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        return orderPaymentOutboxMessage;
    }
    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId,
                                                                       OrderStatus orderStatus,
                                                                       SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                restaurantApprovalOutboxHelp.getOrderApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId),
                        SagaStatus.COMPENSATING);
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            throw new OrderDomainException("Approval outbox message could not be found in " +
                    SagaStatus.COMPENSATING.name() + " status!");
        }
        OrderApprovalOutboxMessage orderApprovalOutboxMessage = orderApprovalOutboxMessageResponse.get();
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }
}
