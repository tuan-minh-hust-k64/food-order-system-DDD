package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentRequestMessageListenerIml implements PaymentRequestMessageListener {
    private final PaymentRequestHelper paymentRequestHelper;

    public PaymentRequestMessageListenerIml(PaymentRequestHelper paymentRequestHelper) {
        this.paymentRequestHelper = paymentRequestHelper;
    }

    @Override
    public void completePayment(PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistPayment(paymentRequest);
        fire(paymentEvent);
    }

    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        PaymentEvent paymentEvent = paymentRequestHelper.persistCancelPayment(paymentRequest);
        fire(paymentEvent);
    }

    private void fire(PaymentEvent paymentEvent) {
        log.info("Publish payment event with order id: {}, customer id: {]", paymentEvent.getPayment().getOrderId().getValue(),
                paymentEvent.getPayment().getCustomerId().getValue());
        paymentEvent.fire();
    }
}
