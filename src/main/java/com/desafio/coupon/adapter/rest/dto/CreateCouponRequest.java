package com.desafio.coupon.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new coupon")
public record CreateCouponRequest(
    
    @Schema(
        description = "Coupon code (6 alphanumeric characters after removing special chars)",
        example = "ABC-123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Code is required")
    String code,
    
    @Schema(
        description = "Coupon description",
        example = "10% discount on all products",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @Schema(
        description = "Discount value (minimum 0.5)",
        example = "10.50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Discount value is required")
    BigDecimal discountValue,
    
    @Schema(
        description = "Expiration date (cannot be in the past)",
        example = "2024-12-31T23:59:59",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Expiration date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime expirationDate,
    
    @Schema(description = "Whether the coupon is published", example = "true", defaultValue = "false")
    Boolean published
) {
    
    public CreateCouponRequest {
        if (published == null) {
            published = false;
        }
    }
}
