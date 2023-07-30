package com.food.ordering.system.event;

public interface DomainEvent<T> {
    void fire();
}
