package com.food.ordering.system.restaurant.service.domain.dto;

import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.valueobject.RestaurantOrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RestaurantApprovalRequest {
    private String id;
    private String sagaId;
    private String restaurantId;
    private String orderId;
    private RestaurantOrderStatus restaurantOrderStatus;
    private List<Product> products;
    private BigDecimal price;
    private Instant createdAt;
}
