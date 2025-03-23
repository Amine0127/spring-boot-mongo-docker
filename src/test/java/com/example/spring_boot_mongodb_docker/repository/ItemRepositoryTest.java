package com.example.spring_boot_mongodb_docker.repository;

import com.example.spring_boot_mongodb_docker.model.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ItemRepositoryTest extends MongoTestContainer {

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();

        testItem = new Item();
        testItem.setName("Test Item");
        testItem.setDescription("This is a test item");
        testItem.setPrice(19.99);
        testItem.setQuantity(10);

        itemRepository.save(testItem);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
    }

    @Test
    void testFindAllItems() {
        List<Item> items = itemRepository.findAll();

        assertEquals(1, items.size());
        assertEquals("Test Item", items.get(0).getName());
        assertEquals(19.99, items.get(0).getPrice());
    }

    @Test
    void testFindItemById() {
        String itemId = testItem.getId();

        Optional<Item> foundItem = itemRepository.findById(itemId);

        assertTrue(foundItem.isPresent());
        assertEquals("Test Item", foundItem.get().getName());
        assertEquals("This is a test item", foundItem.get().getDescription());
    }

    @Test
    void testSaveItem() {
        Item newItem = new Item();
        newItem.setName("New Item");
        newItem.setDescription("This is a new item");
        newItem.setPrice(29.99);
        newItem.setQuantity(5);

        Item savedItem = itemRepository.save(newItem);

        assertNotNull(savedItem.getId());
        assertEquals("New Item", savedItem.getName());

        // Verify it's in the database
        Optional<Item> foundItem = itemRepository.findById(savedItem.getId());
        assertTrue(foundItem.isPresent());
    }

    @Test
    void testUpdateItem() {
        // Get the item
        String itemId = testItem.getId();
        Item itemToUpdate = itemRepository.findById(itemId).orElseThrow();

        // Update it
        itemToUpdate.setName("Updated Item");
        itemToUpdate.setPrice(39.99);

        Item updatedItem = itemRepository.save(itemToUpdate);

        // Verify the update
        assertEquals("Updated Item", updatedItem.getName());
        assertEquals(39.99, updatedItem.getPrice());

        // Verify it's updated in the database
        Optional<Item> foundItem = itemRepository.findById(itemId);
        assertTrue(foundItem.isPresent());
        assertEquals("Updated Item", foundItem.get().getName());
    }

    @Test
    void testDeleteItem() {
        String itemId = testItem.getId();

        // Delete the item
        itemRepository.deleteById(itemId);

        // Verify it's deleted
        Optional<Item> foundItem = itemRepository.findById(itemId);
        assertFalse(foundItem.isPresent());
    }
}

