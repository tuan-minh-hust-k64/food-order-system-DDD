package com.food.ordering.system.restaurant.service.domain;

import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publish.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RestaurantApprovalRequestHelper {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantDataMapper restaurantDataMapper;
    private final OrderApprovalRepository orderApprovalRepository;
    private final RestaurantDomainService restaurantDomainService;
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;
    public RestaurantApprovalRequestHelper(RestaurantRepository restaurantRepository,
                                           RestaurantDataMapper restaurantDataMapper,
                                           OrderApprovalRepository orderApprovalRepository,
                                           RestaurantDomainService restaurantDomainService,
                                           OrderOutboxHelper orderOutboxHelper,
                                           RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantDataMapper = restaurantDataMapper;
        this.orderApprovalRepository = orderApprovalRepository;
        this.restaurantDomainService = restaurantDomainService;
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
    }
    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        if(publishIfOutboxMessageProcessedForPayment(restaurantApprovalRequest)){
            log.info("An outbox message restaurant with saga id: {} is already saved to database!",
                    restaurantApprovalRequest.getSagaId());
            return;
        }
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
        OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(restaurant, failureMessages);
        orderApprovalRepository.save(restaurant.getOrderApproval());
        orderOutboxHelper.saveOrderOutboxMessage(
                restaurantDataMapper.orderApprovalEventToOrderOutboxPayload(orderApprovalEvent),
                orderApprovalEvent.getOrderApproval().getOrderApprovalStatus(),
                OutboxStatus.STARTED,
                UUID.fromString(restaurantApprovalRequest.getSagaId())
        );
    }

    private boolean publishIfOutboxMessageProcessedForPayment(RestaurantApprovalRequest restaurantApprovalRequest) {
        Optional<OrderOutboxMessage> orderOutboxMessageOptional = orderOutboxHelper.getOrderOutboxMessageBySagaIdAndOutboxStatus(UUID.fromString(restaurantApprovalRequest.getSagaId()), OutboxStatus.COMPLETED);
        if(orderOutboxMessageOptional.isPresent()) {
            restaurantApprovalResponseMessagePublisher.publish(
                    orderOutboxMessageOptional.get(),
                    orderOutboxHelper::updateOutboxMessage
            );
            return true;
        }
        return false;
    }
}
