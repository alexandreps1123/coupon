package com.desafio.coupon.application.service;

import com.desafio.coupon.application.dto.CouponDto;
import com.desafio.coupon.application.exception.CouponNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GetCouponServiceTest {

    @Autowired
    private CreateCouponService createCouponService;

    @Autowired
    private DeleteCouponService deleteCouponService;

    @Autowired
    private GetCouponService getCouponService;

    @Test
    void shouldGetCouponByCode() {
        String code = ("G" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto created = createCouponService.execute(
            code,
            "Get coupon",
            new BigDecimal("12.00"),
            LocalDateTime.now().plusDays(10),
            true
        );

        CouponDto found = getCouponService.execute(created.code());

        assertEquals(created.id(), found.id());
        assertEquals(code.toUpperCase(), found.code());
    }

    @Test
    void shouldThrowWhenCouponNotFound() {
        assertThrows(CouponNotFoundException.class, () -> getCouponService.execute("XXXXXX"));
    }

    @Test
    void shouldReturnOnlyActiveCoupons() {
        String activeCode = ("H" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        String deletedCode = ("I" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto active = createCouponService.execute(
            activeCode,
            "Active coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(10),
            false
        );
        CouponDto deleted = createCouponService.execute(
            deletedCode,
            "Deleted coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(10),
            false
        );
        deleteCouponService.execute(deleted.code());

        List<CouponDto> activeCoupons = getCouponService.getAllActive();

        assertTrue(activeCoupons.stream().anyMatch(c -> c.id().equals(active.id())));
        assertTrue(activeCoupons.stream().noneMatch(c -> c.id().equals(deleted.id())));
    }

    @Test
    void shouldReturnAllCouponsIncludingDeleted() {
        String firstCode = ("J" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        String secondCode = ("K" + UUID.randomUUID().toString().replace("-", "")).substring(0, 6);
        CouponDto first = createCouponService.execute(
            firstCode,
            "First coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(10),
            false
        );
        CouponDto second = createCouponService.execute(
            secondCode,
            "Second coupon",
            new BigDecimal("10.00"),
            LocalDateTime.now().plusDays(10),
            false
        );
        deleteCouponService.execute(second.code());

        List<CouponDto> allCoupons = getCouponService.getAll();

        assertTrue(allCoupons.stream().anyMatch(c -> c.id().equals(first.id())));
        assertTrue(allCoupons.stream().anyMatch(c -> c.id().equals(second.id())));
    }
}
