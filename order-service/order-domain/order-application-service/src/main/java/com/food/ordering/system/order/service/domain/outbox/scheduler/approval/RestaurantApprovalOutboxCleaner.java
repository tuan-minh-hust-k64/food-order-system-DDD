package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Component
public class RestaurantApprovalOutboxCleaner implements OutboxScheduler {
    private final RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp;

    public RestaurantApprovalOutboxCleaner(RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp) {
        this.restaurantApprovalOutboxHelp = restaurantApprovalOutboxHelp;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> orderPaymentOutboxMessageList = restaurantApprovalOutboxHelp.getOrderApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED,
                SagaStatus.SUCCEEDED
        );
        if(orderPaymentOutboxMessageList.isPresent()) {
            List<OrderApprovalOutboxMessage> messages = orderPaymentOutboxMessageList.get();
            log.info("Receive {} OrderPaymentOutboxMessage for clean up, Payload: {}",
                    messages.size(),
                    messages.stream().map(OrderApprovalOutboxMessage::getPayload).collect(Collectors.joining("\n"))
            );
            restaurantApprovalOutboxHelp.deleteByTypeAndOutboxStatusAndSagaStatus(
                    OutboxStatus.COMPLETED,
                    SagaStatus.FAILED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.COMPENSATED
            );
        }
    }
}
