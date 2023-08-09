package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.valueobject.CreditHistoryId;
import com.food.ordering.system.payment.service.domain.valueobject.TransactionType;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.PaymentStatus;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class PaymentDomainServiceImpl implements PaymentDomainService{

    @Override
    public PaymentEvent validateAndInitiatePayment(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages, DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventDomainEventPublisher, DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        payment.initializePayment();
        validateCreditEntry(payment, creditEntry, failureMessages);
        subtractCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.DEBIT);
        validateCreditHistory(creditEntry, creditHistories, failureMessages);
        if(failureMessages.isEmpty()) {
            log.info("Payment initial for order id: {}", payment.getOrderId().getValue());
            payment.updatePaymentStatus(PaymentStatus.COMPLETED);
            return new PaymentCompletedEvent(payment, ZonedDateTime.now(ZoneId.of("UTC")), paymentCompletedEventDomainEventPublisher);
        } else {
            log.info("Payment initial failed for order id: {}", payment.getOrderId().getValue());
            payment.updatePaymentStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of("UTC")), failureMessages, paymentFailedEventDomainEventPublisher);
        }
    }

    private void validateCreditHistory(CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages) {
        Money totalCreditHistory = getTotalHistoryAmount(TransactionType.CREDIT, creditHistories);
        Money totalDebitHistory = getTotalHistoryAmount(TransactionType.DEBIT, creditHistories);
        if(totalDebitHistory.isGreatThan(totalCreditHistory)) {
            log.error("Customer with id: {} doesn't have enough credit according to credit history!", creditEntry.getCustomerId().getValue());
            failureMessages.add("Customer with id:" + creditEntry.getCustomerId().getValue()+" doesn't have enough credit according to credit history!");
        }
        if(!creditEntry.getTotalCreditAmount().equals(totalCreditHistory.subMoney(totalDebitHistory))) {
            log.error("Credit history total not equal to current credit for customer id: {}", creditEntry.getCustomerId().getValue());
            failureMessages.add("Credit history total not equal to current credit for customer id: "+ creditEntry.getCustomerId().getValue());
        }
    }
    private Money getTotalHistoryAmount(TransactionType transactionType, List<CreditHistory> creditHistories) {
        return creditHistories.stream()
                .filter(creditHistory -> creditHistory.getTransactionType() == transactionType)
                .map(creditHistory -> creditHistory.getAmount())
                .reduce(Money.ZERO, Money::addMoney);
    }

    private void updateCreditHistory(Payment payment, List<CreditHistory> creditHistories, TransactionType transactionType) {
        creditHistories.add(CreditHistory.builder()
                        .creditHistoryId(new CreditHistoryId(UUID.randomUUID()))
                        .customerId(payment.getCustomerId())
                        .amount(payment.getPrice())
                        .transactionType(transactionType)
                .build());
    }

    private void subtractCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.subtractCreditAmount(payment.getPrice());
    }

    private void validateCreditEntry(Payment payment, CreditEntry creditEntry, List<String> failureMessages) {
        if(payment.getPrice().isGreatThan(creditEntry.getTotalCreditAmount())) {
            log.error("Customer with id: {} not enough credit for payment!", payment.getCustomerId());
            failureMessages.add("Customer with id"+payment.getCustomerId().getValue()+" not enough credit for payment!");
        }
    }

    @Override
    public PaymentEvent validateAndCancelPayment(Payment payment, CreditEntry creditEntry, List<CreditHistory> creditHistories, List<String> failureMessages, DomainEventPublisher<PaymentCancelledEvent> paymentCancelledEventDomainEventPublisher, DomainEventPublisher<PaymentFailedEvent> paymentFailedEventDomainEventPublisher) {
        payment.validatePayment(failureMessages);
        addCreditEntry(payment, creditEntry);
        updateCreditHistory(payment, creditHistories, TransactionType.CREDIT);
        if(failureMessages.isEmpty()) {
            log.info("Payment cancelled for order id: {}", payment.getOrderId().getValue());
            payment.updatePaymentStatus(PaymentStatus.CANCELLED);
            return new PaymentCancelledEvent(payment, ZonedDateTime.now(ZoneId.of("UTC")), paymentCancelledEventDomainEventPublisher);
        } else {
            log.info("Payment cancel failed for order id: {}", payment.getOrderId().getValue());
            payment.updatePaymentStatus(PaymentStatus.FAILED);
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of("UTC")), failureMessages, paymentFailedEventDomainEventPublisher);
        }
    }

    private void addCreditEntry(Payment payment, CreditEntry creditEntry) {
        creditEntry.addCreditAmount(payment.getPrice());
    }
}
