package com.food.ordering.system.restaurant.service.domain.valueobject;

import com.food.ordering.system.valueobject.BaseId;

import java.util.UUID;

public class OrderApprovalId extends BaseId<UUID> {
    protected OrderApprovalId(UUID value) {
        super(value);
    }
}
