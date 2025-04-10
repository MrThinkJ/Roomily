package com.c2se.roomily.repository;

import com.c2se.roomily.payload.response.CheckoutResponse;

import java.util.Optional;

public interface CheckoutInfoRepository {
    CheckoutResponse save(CheckoutResponse checkoutResponse);
    Optional<CheckoutResponse> findById(String checkoutId);
    void delete(String checkoutId);
}
