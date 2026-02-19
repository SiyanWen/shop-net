package org.example.cartservice.service;

import org.example.cartservice.client.AccountServiceClient;
import org.example.cartservice.client.ItemServiceClient;
import org.example.cartservice.client.OrderServiceClient;
import org.example.cartservice.dto.CartDto;
import org.example.cartservice.dto.CreateOrderRequest;
import org.example.cartservice.entity.CartEntity;
import org.example.cartservice.entity.OrderItemEntity;
import org.example.cartservice.repository.CartRepository;
import org.example.cartservice.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ItemServiceClient itemServiceClient;
    @Mock private OrderServiceClient orderServiceClient;
    @Mock private AccountServiceClient accountServiceClient;

    @InjectMocks
    private CartService cartService;

    private CartEntity existingCart;
    private OrderItemEntity existingOrderItem;

    @BeforeEach
    void setUp() {
        existingCart = new CartEntity(1L, "alice", new BigDecimal("9.99"));
        existingOrderItem = new OrderItemEntity(10L, "item-001", 1L, new BigDecimal("9.99"), 1);
    }

    // ------------------------------------------------------------------ addItemToCart

    @Test
    void addItemToCart_newCart_createsCartAndAddsItem() {
        when(cartRepository.getByUserId("alice")).thenReturn(null);
        when(cartRepository.save(any(CartEntity.class)))
                .thenReturn(new CartEntity(1L, "alice", BigDecimal.ZERO));
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("unitPrice", "9.99"));
        when(orderItemRepository.findByCartIdAndItemId(1L, "item-001")).thenReturn(null);

        cartService.addItemToCart("alice", "item-001");

        ArgumentCaptor<OrderItemEntity> itemCaptor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getQuantity()).isEqualTo(1);
        assertThat(itemCaptor.getValue().getPrice()).isEqualByComparingTo("9.99");

        verify(cartRepository).updateTotalPrice(eq(1L), eq(new BigDecimal("9.99")));
    }

    @Test
    void addItemToCart_existingCart_newItem_savesWithQuantityOne() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(itemServiceClient.getItem("item-002"))
                .thenReturn(Map.of("unitPrice", "5.00"));
        when(orderItemRepository.findByCartIdAndItemId(1L, "item-002")).thenReturn(null);

        cartService.addItemToCart("alice", "item-002");

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(1);
        assertThat(captor.getValue().getItemId()).isEqualTo("item-002");
    }

    @Test
    void addItemToCart_existingCart_duplicateItem_incrementsQuantity() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("unitPrice", "9.99"));
        when(orderItemRepository.findByCartIdAndItemId(1L, "item-001"))
                .thenReturn(existingOrderItem);  // already in cart with qty=1

        cartService.addItemToCart("alice", "item-001");

        ArgumentCaptor<OrderItemEntity> captor = ArgumentCaptor.forClass(OrderItemEntity.class);
        verify(orderItemRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);  // 1 → 2
    }

    @Test
    void addItemToCart_updatesTotalPriceWithItemPrice() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart); // totalPrice = 9.99
        when(itemServiceClient.getItem("item-002"))
                .thenReturn(Map.of("unitPrice", "5.00"));
        when(orderItemRepository.findByCartIdAndItemId(1L, "item-002")).thenReturn(null);

        cartService.addItemToCart("alice", "item-002");

        // 9.99 + 5.00 = 14.99
        verify(cartRepository).updateTotalPrice(eq(1L), eq(new BigDecimal("14.99")));
    }

    // ------------------------------------------------------------------ getCart

    @Test
    void getCart_existingCartWithItems_returnsCartDto() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of(existingOrderItem));
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("name", "Burger", "unitPrice", "9.99"));

        CartDto result = cartService.getCart("alice");

        assertThat(result.getCartId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo("alice");
        assertThat(result.getTotalPrice()).isEqualByComparingTo("9.99");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemName()).isEqualTo("Burger");
    }

    @Test
    void getCart_noCart_createsCartAndReturnsEmptyDto() {
        when(cartRepository.getByUserId("alice")).thenReturn(null);
        when(cartRepository.save(any(CartEntity.class)))
                .thenReturn(new CartEntity(1L, "alice", BigDecimal.ZERO));
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of());

        CartDto result = cartService.getCart("alice");

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).save(any(CartEntity.class));
    }

    @Test
    void getCart_itemServiceFails_returnsItemWithEmptyName() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of(existingOrderItem));
        when(itemServiceClient.getItem("item-001"))
                .thenThrow(new RuntimeException("ItemService unavailable"));

        CartDto result = cartService.getCart("alice");

        // Graceful degradation: item still returned, name is empty
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemName()).isEqualTo("");
    }

    // ------------------------------------------------------------------ checkout

    @Test
    void checkout_cartNotFound_throwsIllegalStateException() {
        when(cartRepository.getByUserId("alice")).thenReturn(null);

        assertThatThrownBy(() -> cartService.checkout("alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart not found for user: alice");

        verify(orderServiceClient, never()).createOrder(any());
    }

    @Test
    void checkout_emptyCart_throwsIllegalStateException() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> cartService.checkout("alice"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");

        verify(orderServiceClient, never()).createOrder(any());
    }

    @Test
    void checkout_success_createsOrderAndClearsCart() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of(existingOrderItem));
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("name", "Burger", "unitPrice", "9.99"));
        when(accountServiceClient.getUserByUsername("alice"))
                .thenReturn(Map.of("id", userId.toString()));

        cartService.checkout("alice");

        // Order must be created with correct userId and currency
        ArgumentCaptor<CreateOrderRequest> orderCaptor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(orderServiceClient).createOrder(orderCaptor.capture());
        CreateOrderRequest sentRequest = orderCaptor.getValue();
        assertThat(sentRequest.getUserId()).isEqualTo(userId);
        assertThat(sentRequest.getCurrency()).isEqualTo("USD");
        assertThat(sentRequest.getUsername()).isEqualTo("alice");
        assertThat(sentRequest.getItems()).hasSize(1);
        assertThat(sentRequest.getItems().get(0).getName()).isEqualTo("Burger");

        // Cart must be cleared
        verify(orderItemRepository).deleteByCartId(1L);
        verify(cartRepository).updateTotalPrice(1L, BigDecimal.ZERO);
    }

    @Test
    void checkout_itemServiceFails_stillCreatesOrderWithEmptyName() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of(existingOrderItem));
        when(itemServiceClient.getItem("item-001"))
                .thenThrow(new RuntimeException("ItemService unavailable"));
        when(accountServiceClient.getUserByUsername("alice"))
                .thenReturn(Map.of("id", userId.toString()));

        // Should NOT throw — gracefully uses empty name
        assertThatCode(() -> cartService.checkout("alice")).doesNotThrowAnyException();

        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(orderServiceClient).createOrder(captor.capture());
        assertThat(captor.getValue().getItems().get(0).getName()).isEqualTo("");
    }

    @Test
    void checkout_accountServiceFails_propagatesException() {
        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L)).thenReturn(List.of(existingOrderItem));
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("name", "Burger"));
        when(accountServiceClient.getUserByUsername("alice"))
                .thenThrow(new RuntimeException("AccountService unavailable"));

        assertThatThrownBy(() -> cartService.checkout("alice"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("AccountService unavailable");

        // Order must NOT be created if account lookup fails
        verify(orderServiceClient, never()).createOrder(any());
    }

    @Test
    void checkout_buildsOrderItemsCorrectly() {
        UUID userId = UUID.randomUUID();
        OrderItemEntity item2 = new OrderItemEntity(11L, "item-002", 1L, new BigDecimal("5.00"), 3);

        when(cartRepository.getByUserId("alice")).thenReturn(existingCart);
        when(orderItemRepository.getAllByCartId(1L))
                .thenReturn(List.of(existingOrderItem, item2));
        when(itemServiceClient.getItem("item-001"))
                .thenReturn(Map.of("name", "Burger"));
        when(itemServiceClient.getItem("item-002"))
                .thenReturn(Map.of("name", "Fries"));
        when(accountServiceClient.getUserByUsername("alice"))
                .thenReturn(Map.of("id", userId.toString()));

        cartService.checkout("alice");

        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(orderServiceClient).createOrder(captor.capture());

        var items = captor.getValue().getItems();
        assertThat(items).hasSize(2);
        assertThat(items).extracting("name").containsExactly("Burger", "Fries");
        assertThat(items).extracting("quantity").containsExactly(1, 3);
    }
}
