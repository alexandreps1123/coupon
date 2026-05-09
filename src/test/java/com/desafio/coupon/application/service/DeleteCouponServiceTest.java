package com.desafio.coupon.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;
import com.desafio.coupon.adapter.persistence.repository.CouponRepository;
import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponAlreadyDeletedException;
import com.desafio.coupon.application.exception.CouponNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeleteCouponServiceTest {

    @Autowired
    private CreateCouponService createCouponService;

    @Autowired
    private DeleteCouponService deleteCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void shouldSoftDeleteCoupon() {
        String code = ("D" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto created = createCouponService.execute(
            code,
            "Delete coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(5),
            false
        );

        deleteCouponService.execute(created.code());

        CouponEntity deleted = couponRepository.findByCode(created.code()).orElseThrow();
        assertTrue(deleted.isDeleted());
        assertNotNull(deleted.getDeletedAt());
    }

    @Test
    void shouldThrowWhenDeletingAlreadyDeletedCoupon() {
        String code = ("E" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto created = createCouponService.execute(
            code,
            "Already deleted coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(5),
            false
        );

        deleteCouponService.execute(created.code());

        assertThrows(CouponAlreadyDeletedException.class, () -> deleteCouponService.execute(created.code()));
    }

    @Test
    void shouldThrowWhenCouponIsNotFound() {
        assertThrows(CouponNotFoundException.class, () -> deleteCouponService.execute("XXXXXX"));
    }
}
