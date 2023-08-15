package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RestaurantApprovalRequestHelper {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;
    private final OrderApprovalRepository orderApprovalRepository;
    private final RestaurantDomainService restaurantDomainService;
    private final DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher;
    private final DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher;
    public RestaurantApprovalRequestHelper(RestaurantRepository restaurantRepository,
                                           RestaurantDataMapper restaurantDataMapper,
                                           OrderApprovalRepository orderApprovalRepository,
                                           RestaurantDomainService restaurantDomainService,
                                           DomainEventPublisher<OrderApprovedEvent> orderApprovedEventDomainEventPublisher,
                                           DomainEventPublisher<OrderRejectedEvent> orderRejectedEventDomainEventPublisher) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
        this.orderApprovalRepository = orderApprovalRepository;
        this.restaurantDomainService = restaurantDomainService;
        this.orderApprovedEventDomainEventPublisher = orderApprovedEventDomainEventPublisher;
        this.orderRejectedEventDomainEventPublisher = orderRejectedEventDomainEventPublisher;
    }
    @Transactional
    public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = restaurantDataMapper.restaurantRequestToRestaurantEntity(restaurantApprovalRequest);
        Optional<Restaurant> restaurantResult = restaurantRepository.findRestaurantInformation(restaurant);
        List<String> failureMessages = new ArrayList<>();
        if(restaurantResult.isEmpty()) {
            log.error("Restaurant with id: {} not found", restaurant.getId().getValue());
            throw new RestaurantNotFoundException("Restaurant with id: {} not found" + restaurant.getId().getValue());
        }
        Restaurant restaurantEntity = restaurantResult.get();
        restaurant.setActive(restaurantEntity.isActive());
        restaurant.getOrderDetail().getProducts().forEach((p -> {
            restaurantEntity.getOrderDetail().getProducts().forEach(product -> {
                if(product.getId().equals(p.getId())) {
                    p.updateWithConfirmedNamePriceAndAvailability(product.getName(), product.getPrice(), product.isAvailable());
                }
            });
        }));
        OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(restaurant, failureMessages, orderApprovedEventDomainEventPublisher, orderRejectedEventDomainEventPublisher);
        orderApprovalRepository.save(restaurant.getOrderApproval());
        return  orderApprovalEvent;
    }
}
