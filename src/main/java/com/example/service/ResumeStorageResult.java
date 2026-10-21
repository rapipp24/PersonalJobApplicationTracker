package com.example.service;

public record ResumeStorageResult(
        String storageKey,
        String originalFilename,
        String contentType,
        long fileSize
) {
}
