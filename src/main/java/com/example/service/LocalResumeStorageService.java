package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalResumeStorageService implements ResumeStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalResumeStorageService.class);

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Path storageRoot;

    public LocalResumeStorageService(@Value("${app.storage.resume-dir}") String resumeDirectory) {
        if (resumeDirectory == null || resumeDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("Direktori storage CV tidak boleh kosong");
        }
        this.storageRoot = Paths.get(resumeDirectory).toAbsolutePath().normalize();
    }

    @Override
    public ResumeStorageResult store(
            InputStream inputStream,
            String originalFilename,
            String contentType) {

        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream file CV tidak boleh null");
        }

        String safeOriginalName = sanitizeFilename(originalFilename);
        String extension = extractSafeExtension(safeOriginalName);
        String resolvedContentType = resolveContentType(contentType, extension);

        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membuat direktori storage: " + e.getMessage(), e);
        }

        String storageKey = UUID.randomUUID().toString() + "." + extension;
        Path targetFile = storageRoot.resolve(storageKey).normalize();

        if (!targetFile.startsWith(storageRoot)) {
            throw new SecurityException("Storage key tidak valid");
        }

        long totalBytes = 0;
        try (OutputStream outputStream = Files.newOutputStream(targetFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes > MAX_FILE_SIZE) {
                    throw new IllegalArgumentException("Ukuran file CV tidak boleh melebihi 5 MB");
                }
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception ex) {
            // Hapus partial file jika penulisan gagal atau ukuran melebihi batas
            try {
                Files.deleteIfExists(targetFile);
            } catch (IOException cleanupEx) {
                log.warn("Gagal menghapus partial file: {}", targetFile, cleanupEx);
            }

            if (ex instanceof IllegalArgumentException iae) {
                throw iae;
            }
            throw new IllegalStateException("Gagal menyimpan file CV ke storage: " + ex.getMessage(), ex);
        }

        return new ResumeStorageResult(storageKey, safeOriginalName, resolvedContentType, totalBytes);
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.trim().isEmpty()) {
            return;
        }

        Path target = storageRoot.resolve(storageKey.trim()).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new SecurityException("Akses file di luar storage root ditolak");
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Gagal menghapus file dari storage: {}", target, e);
            throw new IllegalStateException("Gagal menghapus file dari storage: " + e.getMessage(), e);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "cv_document";
        }
        String clean = filename.trim();
        int lastSlash = Math.max(clean.lastIndexOf('/'), clean.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < clean.length() - 1) {
            clean = clean.substring(lastSlash + 1);
        }
        return clean.isBlank() ? "cv_document" : clean;
    }

    private String extractSafeExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama file tidak boleh kosong");
        }
        String cleanFilename = sanitizeFilename(filename).toLowerCase(Locale.ENGLISH);
        if (cleanFilename.endsWith(".pdf")) {
            return "pdf";
        } else if (cleanFilename.endsWith(".docx")) {
            return "docx";
        } else {
            throw new IllegalArgumentException("Format file tidak didukung. Hanya file PDF dan DOCX yang diperbolehkan");
        }
    }

    private String resolveContentType(String contentType, String extension) {
        if (contentType != null && !contentType.trim().isEmpty()) {
            return contentType.trim();
        }
        if ("pdf".equalsIgnoreCase(extension)) {
            return "application/pdf";
        }
        if ("docx".equalsIgnoreCase(extension)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}
