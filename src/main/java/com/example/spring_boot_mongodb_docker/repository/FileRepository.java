package com.example.spring_boot_mongodb_docker.repository;

import com.example.spring_boot_mongodb_docker.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileDocument, String> {
}

