package com.food.ordering.system.order.service.domain.event;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCancelEvent extends OrderEvent{
    private final DomainEventPublisher<OrderCancelEvent> orderCancelEventDomainEventPublisher;
    public OrderCancelEvent(Order order, ZonedDateTime createdAt,
                            DomainEventPublisher<OrderCancelEvent> orderCancelEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCancelEventDomainEventPublisher = orderCancelEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderCancelEventDomainEventPublisher.publish(this);
    }
}
