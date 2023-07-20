package com.food.ordering.system.event.publisher;

import com.food.ordering.system.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {
    void  publish(T domainEvent);
}
