package com.food.ordering.system.restaurant.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderOutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Slf4j
@Component
public class OrderOutboxCleanScheduler implements OutboxScheduler {
    private final OrderOutboxHelper orderOutboxHelper;

    public OrderOutboxCleanScheduler(OrderOutboxHelper orderOutboxHelper) {
        this.orderOutboxHelper = orderOutboxHelper;
    }

    @Override
    @Transactional
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> orderOutboxMessageOptional = orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
        if(orderOutboxMessageOptional.isPresent() && orderOutboxMessageOptional.get().size() > 0) {
            List<OrderOutboxMessage> orderOutboxMessages = orderOutboxMessageOptional.get();
            orderOutboxMessages.forEach(orderOutboxMessage -> {
                orderOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
                log.info("Deleted {} OrderOutboxMessage!", orderOutboxMessageOptional.get().size());
            });
        }
    }
}
