package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.entity.BaseEntity;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntryId;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;

public class CreditEntry extends BaseEntity<CreditEntryId> {
    private final CustomerId customerId;
    private Money totalCreditAmount;

    private CreditEntry(Builder builder) {
        setId(builder.creditEntryId);
        customerId = builder.customerId;
        totalCreditAmount = builder.totalCreditAmount;
    }
    public void addCreditAmount(Money amount) {
        totalCreditAmount = totalCreditAmount.addMoney(amount);
    }
    public void subtractCreditAmount(Money amount) {
        totalCreditAmount = totalCreditAmount.subMoney(amount);
    }
    public static Builder builder() {
        return new Builder();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public static final class Builder {
        private CreditEntryId creditEntryId;
        private CustomerId customerId;
        private Money totalCreditAmount;

        private Builder() {
        }

        public Builder id(CreditEntryId val) {
            creditEntryId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder totalCreditAmount(Money val) {
            totalCreditAmount = val;
            return this;
        }

        public CreditEntry build() {
            return new CreditEntry(this);
        }
    }
}
