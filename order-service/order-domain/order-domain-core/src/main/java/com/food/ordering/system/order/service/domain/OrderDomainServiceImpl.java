package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.exception.DomainException;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService{
    private static final String UTC = "UTC";
    @Override
    public OrderCreateEvent validateAndInitOrder(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        setOrderProductInfomation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreateEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    private void setOrderProductInfomation(Order order, Restaurant restaurant) {
        order.getItems().forEach(orderItem -> {
            restaurant.getProducts().forEach(restaurantProduction -> {
                Product currentProduct = orderItem.getProduct();
                if(currentProduct.equals(restaurantProduction)) {
                    currentProduct.updateWithConfirmNameAndPrice(restaurantProduction.getName(), restaurantProduction.getPrice());
                }
            });
        });
    }

    private void validateRestaurant(Restaurant restaurant) {
        if(!restaurant.isActive()) {
            throw new DomainException("Restaurant is closed!");
        }
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is pay", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order with id: {} is cancelling", order.getId().getValue());
        return new OrderCancelEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id: {} is canceled", order.getId().getValue());
    }

}
