package com.desafio.coupon.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.domain.Coupon;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponCodeAlreadyExistsException;
import com.desafio.coupon.application.mapper.CouponPersistenceMapper;

@Service
public class CreateCouponService {
    
    private final CouponRepository repository;
    private final Clock clock;
    private final CouponPersistenceMapper couponPersistenceMapper;
    private final MeterRegistry meterRegistry;
    
    public CreateCouponService(
        CouponRepository repository,
        Clock clock,
        CouponPersistenceMapper couponPersistenceMapper,
        MeterRegistry meterRegistry
    ) {
        this.repository = repository;
        this.clock = clock;
        this.couponPersistenceMapper = couponPersistenceMapper;
        this.meterRegistry = meterRegistry;
    }
    
    @Transactional
    public CouponDto execute(String code, String description, BigDecimal discountValue,
                         LocalDateTime expirationDate, boolean published) {
        Coupon coupon = Coupon.create(code, description, discountValue, expirationDate, published, clock);
        Optional<CouponEntity> existing = repository.findByCode(coupon.getCode());
        if (existing.isPresent()) {
            return handleExisting(existing.get(), coupon);
        }

        try {
            return Optional.of(coupon)
                .map(couponPersistenceMapper::toEntity)
                .map(repository::save)
                .map(couponPersistenceMapper::toDto)
                .map(dto -> {
                    incrementCreateCounter("created");
                    return dto;
                })
                .orElseThrow();
        } catch (DataIntegrityViolationException ex) {
            return tryResolveConcurrentInsert(coupon, ex);
        }
    }

    private CouponDto tryResolveConcurrentInsert(Coupon coupon, DataIntegrityViolationException original) {
        final int maxAttempts = 5;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Optional<CouponEntity> concurrent = repository.findByCode(coupon.getCode());
            if (concurrent.isPresent()) {
                return handleExisting(concurrent.get(), coupon);
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new CouponCodeAlreadyExistsException(
            String.format("Coupon code %s already exists", coupon.getCode()),
            original
        );
    }

    private CouponDto handleExisting(CouponEntity existing, Coupon requested) {
        if (isIdempotentSameRequest(existing, requested)) {
            incrementCreateCounter("idempotent_hit");
            return couponPersistenceMapper.toDto(existing);
        }
        incrementCreateCounter("conflict_existing_code");
        throw new CouponCodeAlreadyExistsException(
            String.format("Coupon code %s already exists", requested.getCode())
        );
    }

    private boolean isIdempotentSameRequest(CouponEntity existing, Coupon requested) {
        return !existing.isDeleted()
            && existing.getCode().equals(requested.getCode())
            && existing.getDescription().equals(requested.getDescription())
            && existing.getDiscountValue().compareTo(requested.getDiscountValue()) == 0
            && existing.getExpirationDate().equals(requested.getExpirationDate())
            && existing.isPublished() == requested.isPublished();
    }

    private void incrementCreateCounter(String outcome) {
        meterRegistry.counter("coupon.create.requests", "outcome", outcome).increment();
    }
}
