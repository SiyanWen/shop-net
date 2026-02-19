package org.example.orderservice.service;

import feign.FeignException;
import org.example.orderservice.client.AccountServiceClient;
import org.example.orderservice.client.ItemServiceClient;
import org.example.orderservice.dto.CreateOrderRequest;
import org.example.orderservice.dto.OrderItemDto;
import org.example.orderservice.dto.UpdateOrderRequest;
import org.example.orderservice.entity.OrderById;
import org.example.orderservice.entity.OrderByUser;
import org.example.orderservice.entity.OrderByUserKey;
import org.example.orderservice.exception.InvalidOrderStateException;
import org.example.orderservice.exception.InsufficientInventoryException;
import org.example.orderservice.exception.OrderNotFoundException;
import org.example.orderservice.repository.OrderByIdRepository;
import org.example.orderservice.repository.OrderByUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderByIdRepository orderByIdRepository;
    @Mock private OrderByUserRepository orderByUserRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock private ItemServiceClient itemServiceClient;
    @Mock private AccountServiceClient accountServiceClient;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID orderId;
    private OrderById savedOrder;
    private CreateOrderRequest request;
    private OrderItemDto itemDto;

    @BeforeEach
    void setUp() {
        userId  = UUID.randomUUID();
        orderId = UUID.randomUUID();

        itemDto = new OrderItemDto();
        itemDto.setItemId("item-001");
        itemDto.setName("Burger");
        itemDto.setQuantity(2);
        itemDto.setPrice(new BigDecimal("10.00"));

        request = new CreateOrderRequest();
        request.setUserId(userId);
        request.setUsername("alice");
        request.setCurrency("USD");
        request.setItems(List.of(itemDto));

        savedOrder = OrderById.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CREATED")
                .currency("USD")
                .total(new BigDecimal("27.79"))
                .createdAt(Instant.now())
                .build();
    }

    // helper: make a save stub return its argument
    private void stubSaveOrderById() {
        when(orderByIdRepository.save(any(OrderById.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private OrderByUser makeUserOrder(String status) {
        OrderByUserKey key = new OrderByUserKey(userId, Instant.now(), orderId);
        return OrderByUser.builder().key(key).status(status).total(savedOrder.getTotal()).currency("USD").build();
    }

    // ------------------------------------------------------------------ createOrder

    @Test
    void createOrder_success_savesOrderAndPublishesEvent() {
        when(accountServiceClient.getUserByUsername("alice"))
                .thenReturn(Map.of("shippingAddress", "123 Main St"));
        when(itemServiceClient.updateInventory(eq("item-001"), any())).thenReturn(null);
        stubSaveOrderById();
        when(orderByUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderById result = orderService.createOrder(request);

        assertThat(result.getStatus()).isEqualTo("CREATED");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getShippingAddress()).isEqualTo("123 Main St");

        verify(orderByIdRepository).save(any(OrderById.class));
        verify(orderByUserRepository).save(any(OrderByUser.class));
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any());
    }

    @Test
    void createOrder_calculatesSubtotalTaxAndTotal() {
        // price=10.00, qty=2 → subtotal=20.00, tax=1.60, shipping=5.99, total=27.59
        when(accountServiceClient.getUserByUsername("alice")).thenReturn(Map.of());
        when(itemServiceClient.updateInventory(any(), any())).thenReturn(null);
        stubSaveOrderById();
        when(orderByUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderById result = orderService.createOrder(request);

        assertThat(result.getSubtotal()).isEqualByComparingTo("20.00");
        assertThat(result.getTax()).isEqualByComparingTo("1.60");
        assertThat(result.getShippingFee()).isEqualByComparingTo("5.99");
        assertThat(result.getTotal()).isEqualByComparingTo("27.59");
    }

    @Test
    void createOrder_multipleItems_calculatesCorrectTotal() {
        OrderItemDto item2 = new OrderItemDto();
        item2.setItemId("item-002");
        item2.setName("Fries");
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("5.00"));
        request.setItems(List.of(itemDto, item2)); // 20.00 + 5.00 = 25.00

        when(accountServiceClient.getUserByUsername("alice")).thenReturn(Map.of());
        when(itemServiceClient.updateInventory(any(), any())).thenReturn(null);
        stubSaveOrderById();
        when(orderByUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderById result = orderService.createOrder(request);

        // subtotal=25.00, tax=2.00, shipping=5.99, total=32.99
        assertThat(result.getSubtotal()).isEqualByComparingTo("25.00");
        assertThat(result.getTax()).isEqualByComparingTo("2.00");
        assertThat(result.getTotal()).isEqualByComparingTo("32.99");
    }

    @Test
    void createOrder_accountServiceFails_usesEmptyShippingAddress() {
        when(accountServiceClient.getUserByUsername("alice"))
                .thenThrow(new RuntimeException("AccountService unavailable"));
        when(itemServiceClient.updateInventory(any(), any())).thenReturn(null);
        stubSaveOrderById();
        when(orderByUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderById result = orderService.createOrder(request);

        assertThat(result.getShippingAddress()).isEqualTo("");
        assertThat(result.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void createOrder_inventoryFails_throwsInsufficientInventoryException() {
        when(accountServiceClient.getUserByUsername("alice")).thenReturn(Map.of());
        FeignException feignEx = mock(FeignException.class);
        when(itemServiceClient.updateInventory(eq("item-001"), any())).thenThrow(feignEx);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientInventoryException.class)
                .hasMessageContaining("item-001");

        verify(orderByIdRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void createOrder_secondItemFails_rollsBackFirstItem() {
        OrderItemDto item2 = new OrderItemDto();
        item2.setItemId("item-002");
        item2.setName("Fries");
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("5.00"));
        request.setItems(List.of(itemDto, item2));

        when(accountServiceClient.getUserByUsername("alice")).thenReturn(Map.of());
        FeignException feignEx = mock(FeignException.class);
        when(itemServiceClient.updateInventory(eq("item-001"), any())).thenReturn(null);  // succeeds
        when(itemServiceClient.updateInventory(eq("item-002"), any())).thenThrow(feignEx); // fails

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientInventoryException.class);

        // item-001 must be rolled back (quantity restored with positive value)
        ArgumentCaptor<Map> rollbackCaptor = ArgumentCaptor.forClass(Map.class);
        verify(itemServiceClient, times(2)).updateInventory(eq("item-001"), rollbackCaptor.capture());
        // second call to item-001 is the rollback — quantity should be positive (restoring)
        Map<?, ?> rollbackCall = rollbackCaptor.getAllValues().get(1);
        assertThat((Integer) rollbackCall.get("quantity")).isPositive();
    }

    @Test
    void createOrder_mapsItemsCorrectly() {
        when(accountServiceClient.getUserByUsername("alice")).thenReturn(Map.of());
        when(itemServiceClient.updateInventory(any(), any())).thenReturn(null);
        stubSaveOrderById();
        when(orderByUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderById result = orderService.createOrder(request);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getItemId()).isEqualTo("item-001");
        assertThat(result.getItems().get(0).getName()).isEqualTo("Burger");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    // ------------------------------------------------------------------ getOrder

    @Test
    void getOrder_found_returnsOrder() {
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        OrderById result = orderService.getOrder(orderId);

        assertThat(result.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void getOrder_notFound_throwsOrderNotFoundException() {
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ------------------------------------------------------------------ getOrdersByUser

    @Test
    void getOrdersByUser_returnsList() {
        OrderByUser userOrder = makeUserOrder("CREATED");
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of(userOrder));

        List<OrderByUser> result = orderService.getOrdersByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("CREATED");
    }

    @Test
    void getOrdersByUser_noOrders_returnsEmptyList() {
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of());

        assertThat(orderService.getOrdersByUser(userId)).isEmpty();
    }

    // ------------------------------------------------------------------ updateOrder

    @Test
    void updateOrder_createdStatus_updatesAddressesAndSaves() {
        savedOrder.setShippingAddress("old address");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderRequest req = new UpdateOrderRequest();
        req.setShippingAddress("456 Elm St");
        req.setBillingAddress("789 Oak Ave");

        OrderById result = orderService.updateOrder(orderId, req);

        assertThat(result.getShippingAddress()).isEqualTo("456 Elm St");
        assertThat(result.getBillingAddress()).isEqualTo("789 Oak Ave");
        verify(orderByIdRepository).save(savedOrder);
    }

    @Test
    void updateOrder_onlyShippingProvided_doesNotChangeBilling() {
        savedOrder.setBillingAddress("original billing");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderRequest req = new UpdateOrderRequest();
        req.setShippingAddress("new shipping");

        OrderById result = orderService.updateOrder(orderId, req);

        assertThat(result.getShippingAddress()).isEqualTo("new shipping");
        assertThat(result.getBillingAddress()).isEqualTo("original billing");
    }

    @Test
    void updateOrder_notCreatedStatus_throwsInvalidOrderStateException() {
        savedOrder.setStatus("PENDING");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        assertThatThrownBy(() -> orderService.updateOrder(orderId, new UpdateOrderRequest()))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PENDING");

        verify(orderByIdRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ cancelOrder

    @Test
    void cancelOrder_createdStatus_setsStatusAndPublishesEvent() {
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of(makeUserOrder("CREATED")));

        OrderById result = orderService.cancelOrder(orderId);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(orderByIdRepository).save(savedOrder);
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any());
    }

    @Test
    void cancelOrder_alreadyCancelled_returnsWithoutSaving() {
        savedOrder.setStatus("CANCELLED");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        OrderById result = orderService.cancelOrder(orderId);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(orderByIdRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void cancelOrder_pendingStatus_throwsInvalidOrderStateException() {
        savedOrder.setStatus("PENDING");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(orderByIdRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ markOrderPending

    @Test
    void markOrderPending_createdStatus_setsPendingAndPaymentRef() {
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of(makeUserOrder("CREATED")));

        orderService.markOrderPending(orderId, "pay-ref-123");

        ArgumentCaptor<OrderById> captor = ArgumentCaptor.forClass(OrderById.class);
        verify(orderByIdRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().getPaymentRef()).isEqualTo("pay-ref-123");
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any());
    }

    @Test
    void markOrderPending_notCreatedStatus_doesNothing() {
        savedOrder.setStatus("PAID");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        orderService.markOrderPending(orderId, "pay-ref-123");

        verify(orderByIdRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    // ------------------------------------------------------------------ markOrderPaid

    @Test
    void markOrderPaid_pendingStatus_setsPaid() {
        savedOrder.setStatus("PENDING");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of(makeUserOrder("PENDING")));

        orderService.markOrderPaid(orderId, "pay-ref-123");

        ArgumentCaptor<OrderById> captor = ArgumentCaptor.forClass(OrderById.class);
        verify(orderByIdRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("PAID");
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any());
    }

    @Test
    void markOrderPaid_cancelledStatus_doesNothing() {
        savedOrder.setStatus("CANCELLED");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        orderService.markOrderPaid(orderId, "pay-ref-123");

        verify(orderByIdRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    // ------------------------------------------------------------------ markOrderRefunded

    @Test
    void markOrderRefunded_paidStatus_setsRefunded() {
        savedOrder.setStatus("PAID");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
        when(orderByIdRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderByUserRepository.findByKeyUserId(userId)).thenReturn(List.of(makeUserOrder("PAID")));

        orderService.markOrderRefunded(orderId, "pay-ref-123");

        ArgumentCaptor<OrderById> captor = ArgumentCaptor.forClass(OrderById.class);
        verify(orderByIdRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("REFUNDED");
        verify(kafkaTemplate).send(eq("order-events"), anyString(), any());
    }

    @Test
    void markOrderRefunded_notPaidStatus_doesNothing() {
        savedOrder.setStatus("CREATED");
        when(orderByIdRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));

        orderService.markOrderRefunded(orderId, "pay-ref-123");

        verify(orderByIdRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
