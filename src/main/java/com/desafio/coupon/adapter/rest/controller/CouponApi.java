package com.desafio.coupon.adapter.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.desafio.coupon.adapter.rest.dto.CouponResponse;
import com.desafio.coupon.adapter.rest.dto.CreateCouponRequest;

import java.util.List;
@RequestMapping("/api/coupons")
@Tag(name = "Coupons", description = "Coupon management API")
public interface CouponApi {
    
    @PostMapping
    @Operation(summary = "Create a new coupon", description = "Creates a new coupon with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Coupon created successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Coupon code already exists")
    })
    ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CreateCouponRequest request);
    
    @GetMapping("/{code}")
    @Operation(summary = "Get coupon by code", description = "Retrieves a coupon by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coupon found",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
        @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    ResponseEntity<CouponResponse> getCoupon(
            @Parameter(description = "Coupon code", required = true)
            @PathVariable String code);
    
    @GetMapping
    @Operation(summary = "Get all active coupons", description = "Retrieves all non-deleted coupons")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of active coupons",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    })
    ResponseEntity<List<CouponResponse>> getAllCoupons(
            @Parameter(description = "Include deleted coupons")
            @RequestParam(required = false, defaultValue = "false") boolean includeDeleted);
    
    @DeleteMapping("/{code}")
    @Operation(summary = "Delete a coupon", description = "Performs a soft delete on the coupon")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Coupon deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Coupon not found"),
        @ApiResponse(responseCode = "409", description = "Coupon already deleted")
    })
    ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "Coupon code", required = true)
            @PathVariable String code);
}
