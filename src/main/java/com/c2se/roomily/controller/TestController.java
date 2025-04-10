package com.c2se.roomily.controller;

import com.c2se.roomily.payload.response.CheckoutResponse;
import com.c2se.roomily.repository.CheckoutInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    private final CheckoutInfoRepository checkoutInfoRepository;

    @PostMapping("/checkout")
    public void testCheckout() {
        checkoutInfoRepository.save(CheckoutResponse.builder().build());
    }
}
