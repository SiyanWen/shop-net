package org.example.cartservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.cartservice.dto.AddToCartBody;
import org.example.cartservice.dto.CartDto;
import org.example.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartDto getCart(@RequestParam String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping
    public void addToCart(@RequestParam String userId, @RequestBody AddToCartBody body) {
        cartService.addItemToCart(userId, body.getItemId());
    }

    @PostMapping("/checkout")
    public void checkout(@RequestParam String userId) {
        cartService.checkout(userId);
    }
}
