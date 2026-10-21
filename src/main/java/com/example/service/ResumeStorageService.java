package com.example.service;

import java.io.InputStream;

public interface ResumeStorageService {

    ResumeStorageResult store(
            InputStream inputStream,
            String originalFilename,
            String contentType
    );

    void delete(String storageKey);
}
