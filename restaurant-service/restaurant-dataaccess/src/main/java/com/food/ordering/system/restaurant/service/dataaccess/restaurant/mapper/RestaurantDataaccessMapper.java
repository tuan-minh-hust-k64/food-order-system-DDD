package com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.ProductId;
import com.food.ordering.system.valueobject.RestaurantId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RestaurantDataaccessMapper {
    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant.getOrderDetail().getProducts().stream().map(product -> {
            return product.getId().getValue();
        }).collect(Collectors.toList());
    }
    public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = restaurantEntities.stream().findFirst().orElseThrow(() -> {
            return new RestaurantDataAccessException("Restaurant could not be found");
        });
        List<Product> restaurantProducts = restaurantEntities.stream().map(entity -> {
            return Product.builder()
                    .productId(new ProductId(entity.getProductId()))
                    .name(entity.getProductName())
                    .price(new Money(entity.getProductPrice()))
                    .available(entity.getProductAvailable())
                    .build();
        }).collect(Collectors.toList());
        return Restaurant.builder()
                .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
                .orderDetail(OrderDetail.builder()
                        .products(restaurantProducts)
                        .build())
                .active(restaurantEntity.getRestaurantActive())
                .build();
    }
    public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApprovalEntity) {
        return OrderApproval.builder()
                .orderId(new OrderId(orderApprovalEntity.getOrderId()))
                .orderApprovalId(new OrderApprovalId(orderApprovalEntity.getId()))
                .orderApprovalStatus(orderApprovalEntity.getStatus())
                .restaurantId(new RestaurantId(orderApprovalEntity.getRestaurantId()))
                .build();
    }
    public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
        return OrderApprovalEntity.builder()
                .status(orderApproval.getOrderApprovalStatus())
                .orderId(orderApproval.getOrderId().getValue())
                .restaurantId(orderApproval.getRestaurantId().getValue())
                .id(orderApproval.getOrderId().getValue())
                .build();
    }
}
