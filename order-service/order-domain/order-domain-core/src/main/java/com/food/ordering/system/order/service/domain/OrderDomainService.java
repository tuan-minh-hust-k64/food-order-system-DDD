package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {
    OrderCreateEvent validateAndInitOrder(Order order, Restaurant restaurant, DomainEventPublisher<OrderCreateEvent> orderCreateEventDomainEventPublisher);
    OrderPaidEvent payOrder(Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher);
    void approveOrder(Order order);
    OrderCancelEvent cancelOrderPayment(Order order, List<String> failureMessages, DomainEventPublisher<OrderCancelEvent> orderCancelEventDomainEventPublisher);
    void cancelOrder(Order order, List<String> failureMessages);
}
