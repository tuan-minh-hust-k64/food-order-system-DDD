package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.entity.BaseEntity;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.ProductId;

public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;

    public Product(String name, Money price) {
        this.name = name;
        this.price = price;
    }

    public Product(ProductId productId) {
        super.setId(productId);
    }

    public Product(ProductId productId, String name, Money price) {
        super.setId(productId);
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public void updateWithConfirmNameAndPrice(String name, Money price) {
        this.name = name;
        this.price = price;
    }
}
