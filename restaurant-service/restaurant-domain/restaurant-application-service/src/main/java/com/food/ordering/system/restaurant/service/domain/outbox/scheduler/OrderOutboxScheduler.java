package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publish.RestaurantApprovalResponseMessagePublisher;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderOutboxRepository;
import com.food.ordering.system.saga.order.SagaConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class OrderOutboxScheduler implements OutboxScheduler {
    private final OrderOutboxHelper orderOutboxHelper;
    private final RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher;

    public OrderOutboxScheduler(
                                OrderOutboxHelper orderOutboxHelper,
                                RestaurantApprovalResponseMessagePublisher restaurantApprovalResponseMessagePublisher) {
        this.orderOutboxHelper = orderOutboxHelper;
        this.restaurantApprovalResponseMessagePublisher = restaurantApprovalResponseMessagePublisher;
    }

    @Override
    @Scheduled(
            fixedRateString = "${restaurant-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${restaurant-service.outbox-scheduler-initial-delay}"
    )
    @Transactional
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> orderOutboxMessageListOptional = orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(OutboxStatus.STARTED);
        if(orderOutboxMessageListOptional.isPresent() && orderOutboxMessageListOptional.get().size() > 0) {
            List<OrderOutboxMessage> orderOutboxMessagesList = orderOutboxMessageListOptional.get();
            orderOutboxMessagesList.forEach(orderOutboxMessage -> {
                restaurantApprovalResponseMessagePublisher.publish(orderOutboxMessage, orderOutboxHelper::updateOutboxMessage);
            });
        }
    }
}
