package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.valueobject.OrderId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderSagaHelp {
    private final OrderRepository orderRepository;

    public OrderSagaHelp(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findOrder(String orderId) {
        Optional<Order> order = orderRepository.findById(new OrderId(UUID.fromString(orderId)));
        if(order.isEmpty()) {
            log.error("Order with id: {} not found", orderId);
            throw new OrderNotFoundException("Not found order with id: " + orderId);
        }
        return order.get();
    }
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
