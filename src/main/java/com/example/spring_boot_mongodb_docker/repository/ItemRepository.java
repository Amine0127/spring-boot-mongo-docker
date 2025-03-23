package com.example.spring_boot_mongodb_docker.repository;

import com.example.spring_boot_mongodb_docker.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    // For non-paginated queries (can be used for smaller result sets)
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Item> findByNameContainingIgnoreCaseList(String keyword);

    // For paginated queries (should be used for larger result sets)
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
