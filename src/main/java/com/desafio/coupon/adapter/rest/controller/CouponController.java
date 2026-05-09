package com.desafio.coupon.adapter.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.coupon.adapter.rest.dto.CouponResponse;
import com.desafio.coupon.adapter.rest.dto.CreateCouponRequest;
import com.desafio.coupon.adapter.rest.mapper.CouponApiMapper;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.service.CreateCouponService;
import com.desafio.coupon.application.service.DeleteCouponService;
import com.desafio.coupon.application.service.GetCouponService;

@RestController
public class CouponController implements CouponApi {
    
    private final CreateCouponService createCouponService;
    private final DeleteCouponService deleteCouponService;
    private final GetCouponService getCouponService;
    private final CouponApiMapper couponApiMapper;
    
    public CouponController(CreateCouponService createCouponService,
                           DeleteCouponService deleteCouponService,
                           GetCouponService getCouponService,
                           CouponApiMapper couponApiMapper) {
        this.createCouponService = createCouponService;
        this.deleteCouponService = deleteCouponService;
        this.getCouponService = getCouponService;
        this.couponApiMapper = couponApiMapper;
    }
    
    @Override
    public ResponseEntity<CouponResponse> createCoupon(CreateCouponRequest request) {
        CouponDto coupon = createCouponService.execute(
            request.code(),
            request.description(),
            request.discountValue(),
            request.expirationDate(),
            request.published()
        );
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(couponApiMapper.toResponse(coupon));
    }
    
    @Override
    public ResponseEntity<CouponResponse> getCoupon(String code) {
        CouponDto coupon = getCouponService.execute(code);
        return ResponseEntity.ok(couponApiMapper.toResponse(coupon));
    }
    
    @Override
    public ResponseEntity<List<CouponResponse>> getAllCoupons(boolean includeDeleted) {
        List<CouponDto> coupons = includeDeleted
            ? getCouponService.getAll() 
            : getCouponService.getAllActive();

        return ResponseEntity.ok(coupons.stream().map(couponApiMapper::toResponse).toList());
    }
    
    @Override
    public ResponseEntity<Void> deleteCoupon(String code) {
        deleteCouponService.execute(code);
        return ResponseEntity.noContent().build();
    }
}
