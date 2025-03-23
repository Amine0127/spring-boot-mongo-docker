package com.example.spring_boot_mongodb_docker.service;

import com.example.spring_boot_mongodb_docker.model.FileDocument;
import com.example.spring_boot_mongodb_docker.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public String storeFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();
        byte[] data = file.getBytes();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uploadedBy = authentication.getName();

        FileDocument fileDocument = new FileDocument(filename, contentType, size, data, uploadedBy);
        FileDocument savedFile = fileRepository.save(fileDocument);

        return savedFile.getId();
    }

    public FileDocument getFile(String id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
    }

    public List<FileDocument> getAllFiles() {
        List<FileDocument> files = fileRepository.findAll();
        // Remove binary data from the response to reduce payload size
        files.forEach(file -> file.setData(null));
        return files;
    }

    public boolean deleteFile(String id) {
        if (fileRepository.existsById(id)) {
            fileRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
