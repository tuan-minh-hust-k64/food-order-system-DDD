package com.food.ordering.system.order.service.domain.outbox.scheduler.payment;

import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
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
public class PaymentOutboxCleanerScheduler implements OutboxScheduler {
    private final PaymentOutboxHelper paymentOutboxHelper;

    public PaymentOutboxCleanerScheduler(PaymentOutboxHelper paymentOutboxHelper) {
        this.paymentOutboxHelper = paymentOutboxHelper;
    }

    @Override
    @Scheduled(cron = "@midnight")
    public void processOutboxMessage() {
        Optional<List<OrderPaymentOutboxMessage>> orderPaymentOutboxMessageList = paymentOutboxHelper.getPaymentOutboxMessageByOutboxStatusAndSagaStatus(
                OutboxStatus.COMPLETED,
                SagaStatus.FAILED,
                SagaStatus.COMPENSATED,
                SagaStatus.SUCCEEDED
        );
        if(orderPaymentOutboxMessageList.isPresent()) {
            List<OrderPaymentOutboxMessage> messages = orderPaymentOutboxMessageList.get();
            log.info("Receive {} OrderPaymentOutboxMessage for clean up, Payload: {}",
                    messages.size(),
                    messages.stream().map(OrderPaymentOutboxMessage::getPayload).collect(Collectors.joining("\n"))
                    );
            paymentOutboxHelper.deleteByTypeAndOutboxStatusAndSagaStatus(
                    OutboxStatus.COMPLETED,
                    SagaStatus.FAILED,
                    SagaStatus.SUCCEEDED,
                    SagaStatus.COMPENSATED
            );
        }
    }
}
