package com.food.ordering.system.order.service.domain.event;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCreateEvent extends OrderEvent{
    private final DomainEventPublisher<OrderCreateEvent> orderCreateEventDomainEventPublisher;
    public OrderCreateEvent(Order order, ZonedDateTime createdAt,
                            DomainEventPublisher<OrderCreateEvent> orderCreateEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCreateEventDomainEventPublisher = orderCreateEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderCreateEventDomainEventPublisher.publish(this);
    }
}
