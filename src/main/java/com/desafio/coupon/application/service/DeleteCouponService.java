package com.desafio.coupon.application.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.domain.Coupon;
import com.desafio.coupon.application.exception.CouponNotFoundException;
import com.desafio.coupon.application.mapper.CouponPersistenceMapper;

@Service
public class DeleteCouponService {

    private final CouponRepository repository;
    private final Clock clock;
    private final CouponPersistenceMapper couponPersistenceMapper;

    public DeleteCouponService(CouponRepository repository, Clock clock, CouponPersistenceMapper couponPersistenceMapper) {
        this.repository = repository;
        this.clock = clock;
        this.couponPersistenceMapper = couponPersistenceMapper;
    }

    @Transactional
    public void execute(String code) {
        CouponEntity savedCoupon = repository.findByCode(code)
            .orElseThrow(() -> new CouponNotFoundException(
                String.format("Coupon with code %s not found", code)));

        Optional.of(savedCoupon)
            .map(couponPersistenceMapper::toDomain)
            .map(this::delete)
            .map(coupon -> couponPersistenceMapper.toEntity(coupon, savedCoupon))
            .ifPresent(repository::save);
    }

    private Coupon delete(Coupon coupon) {
        return coupon.delete(clock);
    }
}
