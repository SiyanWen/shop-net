package org.example.cartservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cartservice.client.AccountServiceClient;
import org.example.cartservice.client.ItemServiceClient;
import org.example.cartservice.client.OrderServiceClient;
import org.example.cartservice.dto.CartDto;
import org.example.cartservice.dto.CartItemDto;
import org.example.cartservice.dto.CreateOrderRequest;
import org.example.cartservice.dto.OrderItemDto;
import org.example.cartservice.entity.CartEntity;
import org.example.cartservice.entity.OrderItemEntity;
import org.example.cartservice.repository.CartRepository;
import org.example.cartservice.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemServiceClient itemServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public void addItemToCart(String userId, String itemId) {
        CartEntity cart = getOrCreateCart(userId);

        // Fetch item details from ItemService
        Map<String, Object> item = itemServiceClient.getItem(itemId);
        BigDecimal price = new BigDecimal(item.get("unitPrice").toString());

        OrderItemEntity orderItem = orderItemRepository.findByCartIdAndItemId(cart.getId(), itemId);

        if (orderItem == null) {
            orderItem = new OrderItemEntity(null, itemId, cart.getId(), price, 1);
        } else {
            orderItem.setQuantity(orderItem.getQuantity() + 1);
        }

        orderItemRepository.save(orderItem);
        cartRepository.updateTotalPrice(cart.getId(), cart.getTotalPrice().add(price));
    }

    public CartDto getCart(String userId) {
        CartEntity cart = getOrCreateCart(userId);
        List<OrderItemEntity> orderItems = orderItemRepository.getAllByCartId(cart.getId());
        List<CartItemDto> cartItemDtos = buildCartItemDtos(orderItems);

        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setItems(cartItemDtos);
        return dto;
    }

    @Transactional
    public void checkout(String userId) {
        CartEntity cart = cartRepository.getByUserId(userId);
        if (cart == null) {
            throw new IllegalStateException("Cart not found for user: " + userId);
        }

        List<OrderItemEntity> items = orderItemRepository.getAllByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Build order items with names from ItemService
        List<OrderItemDto> orderItems = new ArrayList<>();
        for (OrderItemEntity item : items) {
            String itemName = "";
            try {
                Map<String, Object> itemDetails = itemServiceClient.getItem(item.getItemId());
                itemName = (String) itemDetails.get("name");
            } catch (Exception e) {
                log.warn("Failed to fetch item name for {}: {}", item.getItemId(), e.getMessage());
            }
            orderItems.add(new OrderItemDto(item.getItemId(), itemName, item.getQuantity(), item.getPrice()));
        }

        // Look up user UUID from AccountService
        Map<String, Object> userInfo = accountServiceClient.getUserByUsername(userId);
        UUID userUuid = UUID.fromString(userInfo.get("id").toString());

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(userUuid);
        request.setItems(orderItems);
        request.setCurrency("USD");
        request.setUsername(userId);

        orderServiceClient.createOrder(request);
        log.info("Order created for user {}", userId);

        // Clear the cart after successful order creation
        orderItemRepository.deleteByCartId(cart.getId());
        cartRepository.updateTotalPrice(cart.getId(), BigDecimal.ZERO);
    }

    private CartEntity getOrCreateCart(String userId) {
        CartEntity cart = cartRepository.getByUserId(userId);
        if (cart == null) {
            cart = new CartEntity(null, userId, BigDecimal.ZERO);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    private List<CartItemDto> buildCartItemDtos(List<OrderItemEntity> orderItems) {
        List<CartItemDto> dtos = new ArrayList<>();
        for (OrderItemEntity orderItem : orderItems) {
            String itemName = "";
            try {
                Map<String, Object> item = itemServiceClient.getItem(orderItem.getItemId());
                itemName = (String) item.get("name");
            } catch (Exception e) {
                log.warn("Failed to fetch item details for {}: {}", orderItem.getItemId(), e.getMessage());
            }
            dtos.add(new CartItemDto(
                    orderItem.getId(),
                    orderItem.getItemId(),
                    itemName,
                    orderItem.getPrice(),
                    orderItem.getQuantity()
            ));
        }
        return dtos;
    }
}
