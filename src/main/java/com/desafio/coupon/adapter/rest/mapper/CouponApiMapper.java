package com.desafio.coupon.adapter.rest.mapper;

import org.springframework.stereotype.Component;

import com.desafio.coupon.adapter.rest.dto.CouponResponse;
import com.desafio.coupon.application.dto.CouponDto;

@Component
public class CouponApiMapper {

    public CouponResponse toResponse(CouponDto couponDto) {
        if (couponDto == null) {
            return null;
        }

        return new CouponResponse(
            couponDto.id(),
            couponDto.code(),
            couponDto.description(),
            couponDto.discountValue(),
            couponDto.expirationDate(),
            couponDto.published(),
            couponDto.deleted(),
            couponDto.createdAt(),
            couponDto.deletedAt()
        );
    }
}
