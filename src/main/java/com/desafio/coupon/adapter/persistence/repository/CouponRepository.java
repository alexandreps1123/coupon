package com.desafio.coupon.adapter.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.desafio.coupon.adapter.persistence.entity.CouponEntity;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {
    
    @Query("SELECT c FROM CouponEntity c WHERE c.code = :code")
    Optional<CouponEntity> findByCode(String code);
    
    @Query("SELECT c FROM CouponEntity c WHERE c.deleted = false")
    List<CouponEntity> findAllActive();
    
    @Query("SELECT COUNT(c) > 0 FROM CouponEntity c WHERE c.code = :code")
    boolean existsByCode(String code);
}