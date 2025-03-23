package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.config.MetricsConfig;
import com.example.spring_boot_mongodb_docker.model.Item;
import com.example.spring_boot_mongodb_docker.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MetricsConfig metricsConfig;

    // Create with metrics
    @PostMapping
    @CacheEvict(value = "items", allEntries = true)
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        metricsConfig.getItemCreationCounter().increment();

        Item savedItem = metricsConfig.recordDatabaseOperationTime("createItem", () ->
                itemRepository.save(item)
        );

        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    // Read All with Pagination
    @GetMapping
    @Cacheable(value = "items", key = "#page + '_' + #size + '_' + #sortBy + '_' + #direction")
    public ResponseEntity<Map<String, Object>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Item> pageItems = metricsConfig.recordDatabaseOperationTime("getAllItems", () ->
                itemRepository.findAll(pageable)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageItems.getContent());
        response.put("currentPage", pageItems.getNumber());
        response.put("totalItems", pageItems.getTotalElements());
        response.put("totalPages", pageItems.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // Read One
    @GetMapping("/{id}")
    @Cacheable(value = "items", key = "#id")
    public ResponseEntity<Item> getItemById(@PathVariable String id) {
        Optional<Item> item = metricsConfig.recordDatabaseOperationTime("getItemById", () ->
                itemRepository.findById(id)
        );

        return item.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update
    @PutMapping("/{id}")
    @CacheEvict(value = "items", allEntries = true)
    public ResponseEntity<Item> updateItem(@PathVariable String id, @RequestBody Item itemDetails) {
        return metricsConfig.recordDatabaseOperationTime("updateItem", () -> {
            Optional<Item> optionalItem = itemRepository.findById(id);

            if (optionalItem.isPresent()) {
                Item item = optionalItem.get();
                item.setName(itemDetails.getName());
                item.setDescription(itemDetails.getDescription());
                item.setPrice(itemDetails.getPrice());
                item.setQuantity(itemDetails.getQuantity());

                Item updatedItem = itemRepository.save(item);
                return new ResponseEntity<>(updatedItem, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        });
    }

    // Delete
    @DeleteMapping("/{id}")
    @CacheEvict(value = "items", allEntries = true)
    public ResponseEntity<HttpStatus> deleteItem(@PathVariable String id) {
        try {
            metricsConfig.recordDatabaseOperationTime("deleteItem", () -> {
                itemRepository.deleteById(id);
                return null;
            });
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search with Pagination
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Item> pageItems = metricsConfig.recordDatabaseOperationTime("searchItems", () ->
                itemRepository.findByNameContainingIgnoreCase(keyword, pageable)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageItems.getContent());
        response.put("currentPage", pageItems.getNumber());
        response.put("totalItems", pageItems.getTotalElements());
        response.put("totalPages", pageItems.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
