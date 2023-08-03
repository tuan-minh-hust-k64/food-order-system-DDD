package com.food.ordering.system.payment.service.dataaccess.payment.adapter;

import com.food.ordering.system.payment.service.dataaccess.payment.mapper.PaymentDataaccessMapper;
import com.food.ordering.system.payment.service.dataaccess.payment.repository.PaymentJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;

import java.util.Optional;
import java.util.UUID;

public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentDataaccessMapper paymentDataaccessMapper;

    public PaymentRepositoryImpl(PaymentJpaRepository paymentJpaRepository, PaymentDataaccessMapper paymentDataaccessMapper) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.paymentDataaccessMapper = paymentDataaccessMapper;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentDataaccessMapper.paymentEntityToPayment(paymentJpaRepository.save(
                paymentDataaccessMapper.paymentToPaymentEntity(payment)
        ));
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId).map(paymentDataaccessMapper::paymentEntityToPayment);
    }
}
