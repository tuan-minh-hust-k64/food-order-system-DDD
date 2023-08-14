package com.food.ordering.system.payment.service.domain.outbox.scheduler;

import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
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
    private final PaymentResponseMessagePublisher paymentResponseMessagePublisher;

    public OrderOutboxScheduler(OrderOutboxHelper orderOutboxHelper,
                                PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
        this.orderOutboxHelper = orderOutboxHelper;
        this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
    }

    @Override
    @Transactional
    @Scheduled(
            fixedRateString = "${payment-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${payment-service.outbox-scheduler-initial-delay}"
    )
    public void processOutboxMessage() {
        Optional<List<OrderOutboxMessage>> orderOutboxMessagesOptional = orderOutboxHelper.getOrderOutboxMessageByOutboxStatus(
                OutboxStatus.STARTED
        );
        if(orderOutboxMessagesOptional.isPresent() && orderOutboxMessagesOptional.get().size() > 0) {
            List<OrderOutboxMessage> orderOutboxMessages = orderOutboxMessagesOptional.get();
            orderOutboxMessages.forEach(orderOutboxMessage -> {
                paymentResponseMessagePublisher.publish(orderOutboxMessage, orderOutboxHelper::updateOutboxMessage);
            });
            log.info("{} OrderOutboxMessage sent to message bus!", orderOutboxMessages.size());
        }
    }
}
