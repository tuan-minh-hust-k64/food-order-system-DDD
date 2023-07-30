package com.food.ordering.system.payment.service.dataaccess.payment.mapper;

import com.food.ordering.system.payment.service.dataaccess.payment.entity.PaymentEntity;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataaccessMapper {
    public Payment paymentEntityToPayment(PaymentEntity paymentEntity) {
        return Payment.builder()
                .customerId(new CustomerId(paymentEntity.getCustomerId()))
                .orderId(new OrderId(paymentEntity.getOrderId()))
                .paymentId(new PaymentId(paymentEntity.getId()))
                .price(new Money(paymentEntity.getPrice()))
                .createAt(paymentEntity.getCreatedAt())
                .build();
    }
    public PaymentEntity paymentToPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId().getValue())
                .createdAt(payment.getCreateAt())
                .status(payment.getPaymentStatus())
                .customerId(payment.getCustomerId().getValue())
                .orderId(payment.getOrderId().getValue())
                .price(payment.getPrice().getAmount())
                .build();
    }
}
