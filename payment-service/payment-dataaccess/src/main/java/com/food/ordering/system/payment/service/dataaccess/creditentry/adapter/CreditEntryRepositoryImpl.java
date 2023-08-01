package com.food.ordering.system.payment.service.dataaccess.creditentry.adapter;

import com.food.ordering.system.payment.service.dataaccess.creditentry.mapper.CreditEntryDataaccessMapper;
import com.food.ordering.system.payment.service.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.valueobject.CustomerId;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component

public class CreditEntryRepositoryImpl implements CreditEntryRepository {
    private final CreditEntryDataaccessMapper creditEntryDataaccessMapper;
    private final CreditEntryJpaRepository creditEntryJpaRepository;

    public CreditEntryRepositoryImpl(CreditEntryDataaccessMapper creditEntryDataaccessMapper, CreditEntryJpaRepository creditEntryJpaRepository) {
        this.creditEntryDataaccessMapper = creditEntryDataaccessMapper;
        this.creditEntryJpaRepository = creditEntryJpaRepository;
    }

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        return creditEntryDataaccessMapper.creditEntryEntityToCreditEntry(
                creditEntryJpaRepository.save(creditEntryDataaccessMapper.creditEntryToCreditEntryEntity(creditEntry))
        );
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(CustomerId customerId) {
        return creditEntryJpaRepository.findByCustomerId(customerId).map(creditEntryDataaccessMapper::creditEntryEntityToCreditEntry);
    }
}
