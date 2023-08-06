package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.exception.DomainException;
import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderCreateCommandHelper {
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;
    private final DomainEventPublisher<OrderCreateEvent> orderCreateEventDomainEventPublisher;

    public OrderCreateCommandHelper(OrderDomainService orderDomainService,
                                    OrderRepository orderRepository,
                                    CustomerRepository customerRepository,
                                    RestaurantRepository restaurantRepository,
                                    OrderDataMapper orderDataMapper, DomainEventPublisher<OrderCreateEvent> orderCreateEventDomainEventPublisher) {
        this.orderDomainService = orderDomainService;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
        this.orderCreateEventDomainEventPublisher = orderCreateEventDomainEventPublisher;
    }
    @Transactional
    public OrderCreateEvent persistOrder(CreateOrderCommand createOrderCommand){
        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = checkRestaurant(createOrderCommand);
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        OrderCreateEvent orderCreateEvent = orderDomainService.validateAndInitOrder(order, restaurant, orderCreateEventDomainEventPublisher);
        Order orderRes = saveOrder(order);
        log.info("Create order success with ID: {}", orderRes.getId().getValue());
        return  orderCreateEvent;
    }
    private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findRestaurantInformation(restaurant);
        if(optionalRestaurant.isEmpty()) {
            log.warn("Could not found restaurant with ID: {}", createOrderCommand.getRestaurantId());
            throw new OrderDomainException("Could not found restaurant with ID: {}" + createOrderCommand.getRestaurantId());
        }
        return optionalRestaurant.get();
    }

    private void checkCustomer(UUID customerId) {
        Optional<Customer> customer = customerRepository.findCustomer(customerId);
        if(customer.isEmpty()) {
            log.warn("Could not found customer with id: {}", customerId);
            throw new OrderDomainException("Could not found customer with id: {}" + customerId);
        }
    }

    private Order saveOrder(Order order) {
        Order orderRes = orderRepository.save(order);
        if(orderRes == null) {
            throw new DomainException("Create Order failure");
        }
        log.info("Create order success with ID: {}", orderRes.getId().getValue());
        return orderRes;
    }
}
