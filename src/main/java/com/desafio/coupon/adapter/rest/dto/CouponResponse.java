package com.desafio.coupon.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.desafio.coupon.application.dto.CouponDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Coupon response")
public record CouponResponse(
    
    @Schema(description = "Coupon ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,
    
    @Schema(description = "Coupon code (6 alphanumeric characters)", example = "ABC123")
    String code,
    
    @Schema(description = "Coupon description", example = "10% discount on all products")
    String description,
    
    @Schema(description = "Discount value", example = "10.50")
    BigDecimal discountValue,
    
    @Schema(description = "Expiration date", example = "2024-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expirationDate,
    
    @Schema(description = "Whether the coupon is published", example = "true")
    boolean published,
    
    @Schema(description = "Whether the coupon is deleted", example = "false")
    boolean deleted,
    
    @Schema(description = "Creation date", example = "2024-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @Schema(description = "Deletion date (if deleted)", example = "2024-06-01T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime deletedAt
) {
    
    public static CouponResponse from(CouponDto coupon) {
        return new CouponResponse(
            coupon.id(),
            coupon.code(),
            coupon.description(),
            coupon.discountValue(),
            coupon.expirationDate(),
            coupon.published(),
            coupon.deleted(),
            coupon.createdAt(),
            coupon.deletedAt()
        );
    }
}
