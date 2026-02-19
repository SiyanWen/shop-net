package org.example.itemservice.service;

import org.example.itemservice.entity.Item;
import org.example.itemservice.exception.ItemNotFoundException;
import org.example.itemservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item();
        sampleItem.setId("mongo-id-1");
        sampleItem.setItemId("item-001");
        sampleItem.setName("Burger");
        sampleItem.setUnitPrice(new BigDecimal("9.99"));
        sampleItem.setStockQty(100);
        sampleItem.setCreatedAt(Instant.now());
        sampleItem.setUpdatedAt(Instant.now());
    }

    // ------------------------------------------------------------------ getAllItems

    @Test
    void getAllItems_returnsList() {
        when(itemRepository.findAll()).thenReturn(List.of(sampleItem));

        List<Item> result = itemService.getAllItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemId()).isEqualTo("item-001");
        verify(itemRepository).findAll();
    }

    @Test
    void getAllItems_emptyRepository_returnsEmptyList() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<Item> result = itemService.getAllItems();

        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------ getItems (paginated)

    @Test
    void getItems_returnsPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<Item> page = new PageImpl<>(List.of(sampleItem), pageable, 1);
        when(itemRepository.findAll(pageable)).thenReturn(page);

        Page<Item> result = itemService.getItems(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getItemId()).isEqualTo("item-001");
        verify(itemRepository).findAll(pageable);
    }

    @Test
    void getItems_emptyPage_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 8);
        Page<Item> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(itemRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Item> result = itemService.getItems(pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // ------------------------------------------------------------------ getByItemId

    @Test
    void getByItemId_found_returnsItem() {
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));

        Item result = itemService.getByItemId("item-001");

        assertThat(result.getItemId()).isEqualTo("item-001");
        assertThat(result.getName()).isEqualTo("Burger");
    }

    @Test
    void getByItemId_notFound_throwsItemNotFoundException() {
        when(itemRepository.findByItemId("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getByItemId("bad-id"))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("bad-id");
    }

    // ------------------------------------------------------------------ updateStock

    @Test
    void updateStock_increaseStock_savesAndReturnsUpdatedItem() {
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.updateStock("item-001", 50);

        assertThat(result.getStockQty()).isEqualTo(150);
        verify(itemRepository).save(sampleItem);
    }

    @Test
    void updateStock_decreaseStock_savesAndReturnsUpdatedItem() {
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.updateStock("item-001", -30);

        assertThat(result.getStockQty()).isEqualTo(70);
        verify(itemRepository).save(sampleItem);
    }

    @Test
    void updateStock_exactlyZeroRemaining_savesSuccessfully() {
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.updateStock("item-001", -100);

        assertThat(result.getStockQty()).isEqualTo(0);
        verify(itemRepository).save(sampleItem);
    }

    @Test
    void updateStock_insufficientStock_throwsIllegalArgumentException() {
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> itemService.updateStock("item-001", -101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("100");   // available qty mentioned in message

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateStock_itemNotFound_throwsItemNotFoundException() {
        when(itemRepository.findByItemId("ghost-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateStock("ghost-id", 10))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("ghost-id");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateStock_updatesTimestamp() {
        Instant before = Instant.now();
        when(itemRepository.findByItemId("item-001")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.updateStock("item-001", 1);

        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(before);
    }
}
