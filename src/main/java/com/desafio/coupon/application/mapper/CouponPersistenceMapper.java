package com.desafio.coupon.application.mapper;

import org.springframework.stereotype.Component;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.application.domain.Coupon;
import com.desafio.coupon.application.dto.CouponDto;

@Component
public class CouponPersistenceMapper {

    public CouponEntity toEntity(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return new CouponEntity(
            null,
            coupon.getCode(),
            coupon.getDescription(),
            coupon.getDiscountValue(),
            coupon.getExpirationDate(),
            coupon.isPublished(),
            coupon.isDeleted(),
            null,
            coupon.getDeletedAt()
        );
    }

    public CouponEntity toEntity(Coupon coupon, CouponEntity source) {
        if (coupon == null) {
            return null;
        }
        if (source == null) {
            return toEntity(coupon);
        }

        return new CouponEntity(
            source.getId(),
            coupon.getCode(),
            coupon.getDescription(),
            coupon.getDiscountValue(),
            coupon.getExpirationDate(),
            coupon.isPublished(),
            coupon.isDeleted(),
            source.getCreatedAt(),
            coupon.getDeletedAt()
        );
    }

    public Coupon toDomain(CouponEntity couponEntity) {
        if (couponEntity == null) {
            return null;
        }

        return Coupon.builder()
            .code(couponEntity.getCode())
            .description(couponEntity.getDescription())
            .discountValue(couponEntity.getDiscountValue())
            .expirationDate(couponEntity.getExpirationDate())
            .published(couponEntity.isPublished())
            .deleted(couponEntity.isDeleted())
            .deletedAt(couponEntity.getDeletedAt())
            .build();
    }

    public CouponDto toDto(CouponEntity couponEntity) {
        if (couponEntity == null) {
            return null;
        }

        return new CouponDto(
            couponEntity.getId(),
            couponEntity.getCode(),
            couponEntity.getDescription(),
            couponEntity.getDiscountValue(),
            couponEntity.getExpirationDate(),
            couponEntity.isPublished(),
            couponEntity.isDeleted(),
            couponEntity.getCreatedAt(),
            couponEntity.getDeletedAt()
        );
    }
}
