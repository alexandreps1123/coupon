package com.desafio.coupon.application.domain;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import com.desafio.coupon.application.exception.CouponAlreadyDeletedException;
import com.desafio.coupon.application.exception.InvalidCouponCodeException;
import com.desafio.coupon.application.exception.InvalidCouponDescriptionException;
import com.desafio.coupon.application.exception.InvalidDiscountValueException;
import com.desafio.coupon.application.exception.InvalidExpirationDateException;

public final class Coupon {
    private static final int REQUIRED_LENGTH = 6;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final String SPECIAL_CHARS_REGEX = "[^a-zA-Z0-9]";
    private static final BigDecimal MINIMUM_VALUE = new BigDecimal("0.5");

    private final String code;
    private final String description;
    private final BigDecimal discountValue;
    private final LocalDateTime expirationDate;
    private final boolean published;
    private final boolean deleted;
    private final LocalDateTime deletedAt;

    private Coupon(String code, String description, BigDecimal discountValue,
                   LocalDateTime expirationDate, boolean published, boolean deleted, LocalDateTime deletedAt) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
    }

    public static Coupon create(String rawCode, String rawDescription, BigDecimal rawDiscountValue,
                                LocalDateTime rawExpirationDate, boolean published, Clock clock) {
        String normalizedCode = validateCode(normalizeCode(rawCode));
        String description = validateDescription(rawDescription);
        BigDecimal discountValue = validateDiscountValue(rawDiscountValue);
        LocalDateTime expirationDate = validateExpirationDate(rawExpirationDate, clock);

        return builder()
            .code(normalizedCode)
            .description(description)
            .discountValue(discountValue)
            .expirationDate(expirationDate)
            .published(published)
            .deleted(false)
            .deletedAt(null)
            .build();
    }

    public Coupon delete(Clock clock) {
        if (this.deleted) {
            throw new CouponAlreadyDeletedException(
                String.format("Coupon with code %s is already deleted", this.code)
            );
        }

        return builder()
            .code(code)
            .description(description)
            .discountValue(discountValue)
            .expirationDate(expirationDate)
            .published(published)
            .deleted(true)
            .deletedAt(LocalDateTime.now(clock))
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidCouponCodeException("Coupon code cannot be null or empty");
        }
        return code.replaceAll(SPECIAL_CHARS_REGEX, "").toUpperCase(Locale.ROOT);
    }

    private static String validateCode(String normalizedCode) {
        if (normalizedCode.length() != REQUIRED_LENGTH) {
            throw new InvalidCouponCodeException(
                String.format("Coupon code must have exactly %d alphanumeric characters after removing special characters. Got: %d",
                    REQUIRED_LENGTH, normalizedCode.length())
            );
        }

        return normalizedCode;
    }

    private static String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidCouponDescriptionException("Description cannot be null or empty");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidCouponDescriptionException(
                String.format("Description must not exceed %d characters", MAX_DESCRIPTION_LENGTH)
            );
        }

        return description;
    }

    private static BigDecimal validateDiscountValue(BigDecimal value) {
        if (value == null) {
            throw new InvalidDiscountValueException("Discount value cannot be null");
        }

        if (value.compareTo(MINIMUM_VALUE) < 0) {
            throw new InvalidDiscountValueException(
                String.format("Discount value must be at least %s. Got: %s",
                    MINIMUM_VALUE, value)
            );
        }

        return value;
    }

    private static LocalDateTime validateExpirationDate(LocalDateTime expirationDate, Clock clock) {
        if (expirationDate == null) {
            throw new InvalidExpirationDateException("Expiration date cannot be null");
        }
        if (clock == null) {
            throw new InvalidExpirationDateException("Clock cannot be null");
        }
        if (expirationDate.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidExpirationDateException(
                "Coupon cannot be created with expiration date in the past"
            );
        }

        return expirationDate;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coupon coupon)) return false;
        return published == coupon.published
            && deleted == coupon.deleted
            && Objects.equals(code, coupon.code)
            && Objects.equals(description, coupon.description)
            && Objects.equals(discountValue, coupon.discountValue)
            && Objects.equals(expirationDate, coupon.expirationDate)
            && Objects.equals(deletedAt, coupon.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, description, discountValue, expirationDate, published, deleted, deletedAt);
    }

    @Override
    public String toString() {
        return "Coupon{" +
            "code='" + code + '\'' +
            ", description='" + description + '\'' +
            ", discountValue=" + discountValue +
            ", expirationDate=" + expirationDate +
            ", published=" + published +
            ", deleted=" + deleted +
            ", deletedAt=" + deletedAt +
            '}';
    }

    public static final class Builder {
        private String code;
        private String description;
        private BigDecimal discountValue;
        private LocalDateTime expirationDate;
        private boolean published;
        private boolean deleted;
        private LocalDateTime deletedAt;

        private Builder() {
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder discountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
            return this;
        }

        public Builder expirationDate(LocalDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            return this;
        }

        public Builder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Coupon build() {
            return new Coupon(code, description, discountValue, expirationDate, published, deleted, deletedAt);
        }
    }
}
