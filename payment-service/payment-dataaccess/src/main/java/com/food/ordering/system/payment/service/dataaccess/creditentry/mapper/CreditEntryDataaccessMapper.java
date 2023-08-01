package com.food.ordering.system.payment.service.dataaccess.creditentry.mapper;

import com.food.ordering.system.payment.service.dataaccess.creditentry.entity.CreditEntryEntity;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.valueobject.CreditEntryId;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;
import org.springframework.stereotype.Component;

@Component
public class CreditEntryDataaccessMapper {
    public CreditEntry creditEntryEntityToCreditEntry(CreditEntryEntity creditEntryEntity) {

        return CreditEntry.builder()
                .creditEntryId(new CreditEntryId(creditEntryEntity.getId()))
                .customerId(new CustomerId(creditEntryEntity.getCustomerId()))
                .totalCreditAmount(new Money(creditEntryEntity.getTotalCreditAmount()))
                .build();
    }
    public CreditEntryEntity creditEntryToCreditEntryEntity(CreditEntry creditEntry) {
        return  CreditEntryEntity.builder()
                .totalCreditAmount(creditEntry.getTotalCreditAmount().getAmount())
                .id(creditEntry.getId().getValue())
                .customerId(creditEntry.getCustomerId().getValue())
                .build();
    }
}
