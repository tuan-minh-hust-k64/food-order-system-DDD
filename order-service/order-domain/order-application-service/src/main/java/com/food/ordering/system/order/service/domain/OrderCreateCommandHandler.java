package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.event.payload.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class OrderCreateCommandHandler {
    private final OrderDataMapper orderDataMapper;
    private final PaymentOutboxHelper paymentOutboxHelper;

    private final OrderCreateCommandHelper orderCreateCommandHelper;
    private final OrderSagaHelp orderSagaHelp;
    public OrderCreateCommandHandler(OrderDataMapper orderDataMapper,
                                     PaymentOutboxHelper paymentOutboxHelper,
                                     OrderCreateCommandHelper orderCreateCommandHelper,
                                     OrderSagaHelp orderSagaHelp) {
        this.orderDataMapper = orderDataMapper;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.orderCreateCommandHelper = orderCreateCommandHelper;
        this.orderSagaHelp = orderSagaHelp;
    }
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreateEvent orderCreateEvent = orderCreateCommandHelper.persistOrder(createOrderCommand);
        log.info("Order is created with id: {}", orderCreateEvent.getOrder().getId().getValue());
        OrderPaymentEventPayload orderPaymentEventPayload = orderDataMapper.orderCreateEventToOrderPaymentEventPayload(orderCreateEvent);
        paymentOutboxHelper.savePaymentOutboxMessage(
                orderPaymentEventPayload,
                orderCreateEvent.getOrder().getOrderStatus(),
                orderSagaHelp.orderStatusToSagaStatus(orderCreateEvent.getOrder().getOrderStatus()),
                OutboxStatus.STARTED,
                UUID.randomUUID()
        );
        log.info("Returning CreateOrderResponse with order id: {}", orderCreateEvent.getOrder().getId());

        return orderDataMapper.orderToCreateOrderResponse(orderCreateEvent.getOrder());
    }


}
