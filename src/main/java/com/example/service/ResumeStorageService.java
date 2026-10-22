package com.example.service;

import java.io.InputStream;

public interface ResumeStorageService {

    ResumeStorageResult store(
            InputStream inputStream,
            String originalFilename,
            String contentType
    );

    InputStream load(String storageKey);

    void delete(String storageKey);
}
