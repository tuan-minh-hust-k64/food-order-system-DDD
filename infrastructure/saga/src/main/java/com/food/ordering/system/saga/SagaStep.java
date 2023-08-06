package com.food.ordering.system.saga;

import com.food.ordering.system.event.DomainEvent;

public interface SagaStep<T, S extends DomainEvent, U extends DomainEvent>{
    S process(T data);
    U rollback(T data);
}
