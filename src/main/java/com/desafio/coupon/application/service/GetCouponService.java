package com.desafio.coupon.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponNotFoundException;
import com.desafio.coupon.application.mapper.CouponPersistenceMapper;

@Service
public class GetCouponService {
    
    private final CouponRepository repository;
    private final CouponPersistenceMapper couponPersistenceMapper;
    
    public GetCouponService(CouponRepository repository, CouponPersistenceMapper couponPersistenceMapper) {
        this.repository = repository;
        this.couponPersistenceMapper = couponPersistenceMapper;
    }
    
    @Transactional(readOnly = true)
    public CouponDto execute(String code) {
        return repository.findByCode(code)
            .map(couponPersistenceMapper::toDto)
            .orElseThrow(() -> new CouponNotFoundException(
                String.format("Coupon with code %s not found", code)
            ));
    }
    
    @Transactional(readOnly = true)
    public List<CouponDto> getAllActive() {
        return repository.findAllActive().stream()
            .map(couponPersistenceMapper::toDto)
            .toList();
    }
    
    @Transactional(readOnly = true)
    public List<CouponDto> getAll() {
        return repository.findAll().stream()
            .map(couponPersistenceMapper::toDto)
            .toList();
    }
}
