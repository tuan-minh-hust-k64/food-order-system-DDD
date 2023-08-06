package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderTrackCommandHandler {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    private final OrderDataMapper orderDataMapper;

    public OrderTrackCommandHandler(OrderRepository orderRepository, CustomerRepository customerRepository, OrderDataMapper orderDataMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderDataMapper = orderDataMapper;
    }
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> order = orderRepository.findByTrackingId(new TrackingId(trackOrderQuery.getOrderTrackingId()));
        if(order.isEmpty()) {
            log.info("Could not found order with tracking ID: {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not found order with ID: {}" + trackOrderQuery.getOrderTrackingId());
        }
        TrackOrderResponse trackOrderResponse = orderDataMapper.orderToTrackOrderResponse(order.get());
        return  trackOrderResponse;
    }
 }
