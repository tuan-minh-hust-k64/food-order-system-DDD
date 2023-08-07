package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.event.EmptyEvent;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderApprovalSaga implements SagaStep<RestaurantApprovalResponse, EmptyEvent, OrderCancelEvent> {
    private final OrderDomainService orderDomainService;
    private final OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher;
    private final OrderSagaHelp orderSagaHelp;

    public OrderApprovalSaga(OrderDomainService orderDomainService,
                             OrderCancelledPaymentRequestMessagePublisher orderCancelledPaymentRequestMessagePublisher,
                             OrderSagaHelp orderSagaHelp) {
        this.orderDomainService = orderDomainService;
        this.orderCancelledPaymentRequestMessagePublisher = orderCancelledPaymentRequestMessagePublisher;
        this.orderSagaHelp = orderSagaHelp;
    }

    @Override
    public EmptyEvent process(RestaurantApprovalResponse data) {
        log.info("Order approval success with id: {}", data.getOrderId());
        Order order = orderSagaHelp.findOrder(data.getOrderId());
        orderDomainService.approveOrder(order);
        orderSagaHelp.saveOrder(order);
        return EmptyEvent.INSTANCE;
    }

    @Override
    public OrderCancelEvent rollback(RestaurantApprovalResponse data) {
        log.info("Cancelled order with id: {}", data.getOrderId());
        Order order = orderSagaHelp.findOrder(data.getOrderId());
        OrderCancelEvent orderCancelEvent = orderDomainService.cancelOrderPayment(
                order,
                data.getFailureMessages(),
                orderCancelledPaymentRequestMessagePublisher);
        orderSagaHelp.saveOrder(order);
        return orderCancelEvent;
    }
}
