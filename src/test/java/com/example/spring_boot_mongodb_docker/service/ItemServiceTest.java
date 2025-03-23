package com.example.spring_boot_mongodb_docker.service;

import com.example.spring_boot_mongodb_docker.model.Item;
import com.example.spring_boot_mongodb_docker.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId("1");
        testItem.setName("Test Item");
        testItem.setDescription("This is a test item");
        testItem.setPrice(19.99);
        testItem.setQuantity(10);
    }

    @Test
    void testFindAllItems() {
        // Arrange
        when(itemRepository.findAll()).thenReturn(Arrays.asList(testItem));

        // Act
        List<Item> items = itemService.getAllItems();

        // Assert
        assertEquals(1, items.size());
        assertEquals("Test Item", items.get(0).getName());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testFindItemById() {
        // Arrange
        when(itemRepository.findById("1")).thenReturn(Optional.of(testItem));

        // Act
        Optional<Item> foundItem = itemService.getItemById("1");

        // Assert
        assertTrue(foundItem.isPresent());
        assertEquals("Test Item", foundItem.get().getName());
        verify(itemRepository, times(1)).findById("1");
    }

    @Test
    void testCreateItem() {
        // Arrange
        Item newItem = new Item();
        newItem.setName("New Item");
        newItem.setDescription("This is a new item");
        newItem.setPrice(29.99);
        newItem.setQuantity(5);

        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Act
        Item savedItem = itemService.createItem(newItem);

        // Assert
        assertNotNull(savedItem);
        assertEquals("Test Item", savedItem.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testUpdateItem() {
        // Arrange
        Item updatedItem = new Item();
        updatedItem.setId("1");
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("This is an updated item");
        updatedItem.setPrice(39.99);
        updatedItem.setQuantity(15);

        when(itemRepository.findById("1")).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        // Act
        Optional<Item> result = itemService.updateItem("1", updatedItem);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Item", result.get().getName());
        assertEquals(39.99, result.get().getPrice());
        verify(itemRepository, times(1)).findById("1");
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testDeleteItem() {
        // Arrange
        when(itemRepository.findById("1")).thenReturn(Optional.of(testItem));
        doNothing().when(itemRepository).deleteById("1");

        // Act
        boolean result = itemService.deleteItem("1");

        // Assert
        assertTrue(result);
        verify(itemRepository, times(1)).findById("1");
        verify(itemRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteItemNotFound() {
        // Arrange
        when(itemRepository.findById("999")).thenReturn(Optional.empty());

        // Act
        boolean result = itemService.deleteItem("999");

        // Assert
        assertFalse(result);
        verify(itemRepository, times(1)).findById("999");
        verify(itemRepository, never()).deleteById(anyString());
    }
}

