package com.desafio.coupon.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    public CreateCouponService(CouponRepository repository, Clock clock, CouponPersistenceMapper couponPersistenceMapper) {
        this.repository = repository;
        this.clock = clock;
        this.couponPersistenceMapper = couponPersistenceMapper;
    }
    
    @Transactional
    public CouponDto execute(String code, String description, BigDecimal discountValue,
                         LocalDateTime expirationDate, boolean published) {
        Coupon coupon = Coupon.create(code, description, discountValue, expirationDate, published, clock);
        if (repository.existsByCode(coupon.getCode())) {
            throw new CouponCodeAlreadyExistsException(
                String.format("Coupon code %s already exists", coupon.getCode())
            );
        }
        
        return Optional.of(coupon)
            .map(couponPersistenceMapper::toEntity)
            .map(repository::save)
            .map(couponPersistenceMapper::toDto)
            .orElseThrow();
    }
}
