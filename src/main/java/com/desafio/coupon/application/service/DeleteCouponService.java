package com.desafio.coupon.application.service;

import java.time.Clock;
import java.util.Optional;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.domain.Coupon;
import com.desafio.coupon.application.exception.CouponAlreadyDeletedException;
import com.desafio.coupon.application.exception.CouponNotFoundException;
import com.desafio.coupon.application.mapper.CouponPersistenceMapper;

@Service
public class DeleteCouponService {

    private final CouponRepository repository;
    private final Clock clock;
    private final CouponPersistenceMapper couponPersistenceMapper;
    private final MeterRegistry meterRegistry;

    public DeleteCouponService(
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
    public void execute(String code) {
        CouponEntity savedCoupon = repository.findByCode(code)
            .orElseThrow(() -> {
                incrementDeleteCounter("not_found");
                return new CouponNotFoundException(String.format("Coupon with code %s not found", code));
            });

        try {
            Optional.of(savedCoupon)
                .map(couponPersistenceMapper::toDomain)
                .map(this::delete)
                .map(coupon -> couponPersistenceMapper.toEntity(coupon, savedCoupon))
                .ifPresent(repository::save);
            incrementDeleteCounter("deleted");
        } catch (CouponAlreadyDeletedException ex) {
            incrementDeleteCounter("already_deleted");
            throw ex;
        }
    }

    private Coupon delete(Coupon coupon) {
        return coupon.delete(clock);
    }

    private void incrementDeleteCounter(String outcome) {
        meterRegistry.counter("coupon.delete.requests", "outcome", outcome).increment();
    }
}
