package com.example.spring_boot_mongodb_docker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "files")
public class FileDocument {

    @Id
    private String id;
    private String filename;
    private String contentType;
    private long size;
    private Date uploadDate;
    private byte[] data;
    private String uploadedBy;

    public FileDocument() {
    }

    public FileDocument(String filename, String contentType, long size, byte[] data, String uploadedBy) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
        this.uploadDate = new Date();
        this.uploadedBy = uploadedBy;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date uploadDate) { this.uploadDate = uploadDate; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
