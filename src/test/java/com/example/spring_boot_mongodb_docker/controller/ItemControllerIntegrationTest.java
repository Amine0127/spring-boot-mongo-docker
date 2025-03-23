package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.Item;
import com.example.spring_boot_mongodb_docker.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerIntegrationTest extends MongoTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testGetAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")))
                .andExpect(jsonPath("$[0].price", is(19.99)));
    }

    @Test
    void testGetItemById() throws Exception {
        String itemId = testItem.getId();

        mockMvc.perform(get("/api/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Test Item")))
                .andExpect(jsonPath("$.description", is("This is a test item")))
                .andExpect(jsonPath("$.price", is(19.99)))
                .andExpect(jsonPath("$.quantity", is(10)));
    }

    @Test
    void testGetItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/items/nonexistentid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateItem() throws Exception {
        Item newItem = new Item();
        newItem.setName("New Item");
        newItem.setDescription("This is a new item");
        newItem.setPrice(29.99);
        newItem.setQuantity(5);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("New Item")))
                .andExpect(jsonPath("$.description", is("This is a new item")))
                .andExpect(jsonPath("$.price", is(29.99)))
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    void testUpdateItem() throws Exception {
        String itemId = testItem.getId();

        Item updatedItem = new Item();
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("This is an updated item");
        updatedItem.setPrice(39.99);
        updatedItem.setQuantity(15);

        mockMvc.perform(put("/api/items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Updated Item")))
                .andExpect(jsonPath("$.description", is("This is an updated item")))
                .andExpect(jsonPath("$.price", is(39.99)))
                .andExpect(jsonPath("$.quantity", is(15)));
    }

    @Test
    void testUpdateItemNotFound() throws Exception {
        Item updatedItem = new Item();
        updatedItem.setName("Updated Item");
        updatedItem.setDescription("This is an updated item");
        updatedItem.setPrice(39.99);
        updatedItem.setQuantity(15);

        mockMvc.perform(put("/api/items/nonexistentid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteItem() throws Exception {
        String itemId = testItem.getId();

        mockMvc.perform(delete("/api/items/{id}", itemId))
                .andExpect(status().isNoContent());

        // Verify item is deleted
        mockMvc.perform(get("/api/items/{id}", itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteItemNotFound() throws Exception {
        mockMvc.perform(delete("/api/items/nonexistentid"))
                .andExpect(status().isNotFound());
    }
}
