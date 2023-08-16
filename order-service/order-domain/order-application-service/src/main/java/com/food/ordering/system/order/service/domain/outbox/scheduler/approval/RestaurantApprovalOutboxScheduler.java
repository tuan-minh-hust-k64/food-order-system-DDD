package com.food.ordering.system.order.service.domain.outbox.scheduler.approval;

import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.RestaurantApprovalRequestMessagePublisher;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RestaurantApprovalOutboxScheduler implements OutboxScheduler {
    private final RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp;
    private final RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher;

    public RestaurantApprovalOutboxScheduler(RestaurantApprovalOutboxHelp restaurantApprovalOutboxHelp,
                                             RestaurantApprovalRequestMessagePublisher restaurantApprovalRequestMessagePublisher) {
        this.restaurantApprovalOutboxHelp = restaurantApprovalOutboxHelp;
        this.restaurantApprovalRequestMessagePublisher = restaurantApprovalRequestMessagePublisher;
    }

    @Override
    @Scheduled(fixedDelayString = "${order-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${order-service.outbox-scheduler-initial-delay}")
    @Transactional
    public void processOutboxMessage() {
        Optional<List<OrderApprovalOutboxMessage>> outboxMessages = restaurantApprovalOutboxHelp.getOrderApprovalOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.STARTED,
                SagaStatus.PROCESSING
        );
        if (outboxMessages.isPresent() && outboxMessages.get().size() > 0) {
            List<OrderApprovalOutboxMessage> outboxMessageList = outboxMessages.get();
            log.info("Receive {} OrderPaymentOutbox with ids: {}, sending to message bus!",
                    outboxMessageList.size(),
                    outboxMessageList.stream().map(outboxMessage -> {
                        return outboxMessage.getId().toString();
                    }).collect(Collectors.joining(", "))
            );
            outboxMessageList.forEach(item -> {
                restaurantApprovalRequestMessagePublisher.publish(item, this::updateOutboxStatus);
                log.info("{} OrderPaymentOutboxMessage is sent to message bus", outboxMessageList.size());
            });
        }
    }

    private void updateOutboxStatus(OrderApprovalOutboxMessage orderApprovalOutboxMessage, OutboxStatus outboxStatus) {
        orderApprovalOutboxMessage.setOutboxStatus(outboxStatus);
        restaurantApprovalOutboxHelp.save(orderApprovalOutboxMessage);
        log.info("OrderPaymentOutboxMessage is updated with outbox status: {}", outboxStatus.name());
    }
}
