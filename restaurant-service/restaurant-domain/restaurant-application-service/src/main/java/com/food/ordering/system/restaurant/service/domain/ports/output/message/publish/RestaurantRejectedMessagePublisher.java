package com.food.ordering.system.restaurant.service.domain.ports.output.message.publish;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;

public interface RestaurantRejectedMessagePublisher extends DomainEventPublisher<OrderRejectedEvent> {
}
