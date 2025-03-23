package com.example.spring_boot_mongodb_docker.controller;

import com.example.spring_boot_mongodb_docker.model.FileDocument;
import com.example.spring_boot_mongodb_docker.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String id = fileService.storeFile(file);
            return ResponseEntity.ok("File uploaded successfully with ID: " + id);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Could not upload file: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<FileDocument>> getAllFiles() {
        List<FileDocument> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable String id) {
        FileDocument fileDocument = fileService.getFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDocument.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDocument.getFilename() + "\"")
                .body(new ByteArrayResource(fileDocument.getData()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteFile(@PathVariable String id) {
        boolean deleted = fileService.deleteFile(id);
        if (deleted) {
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
