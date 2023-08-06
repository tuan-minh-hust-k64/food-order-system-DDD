package com.food.ordering.system.restaurant.service.domain.ports.output.message.publish;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;

public interface RestaurantApprovedMessagePublisher extends DomainEventPublisher<OrderApprovedEvent> {
}
