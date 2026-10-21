package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalResumeStorageServiceTest {

    @TempDir
    Path tempStorageDir;

    private LocalResumeStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalResumeStorageService(tempStorageDir.toString());
    }

    @Test
    void harusBisaMenyimpanFileCvKePrivateStorage() throws IOException {
        String content = "Contoh isi CV Pelamar dalam format PDF";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(bytes);

        ResumeStorageResult result = storageService.store(
                stream,
                "Curriculum_Vitae.pdf",
                "application/pdf"
        );

        assertNotNull(result);
        assertNotNull(result.storageKey());
        assertTrue(result.storageKey().endsWith(".pdf"));
        assertEquals("Curriculum_Vitae.pdf", result.originalFilename());
        assertEquals("application/pdf", result.contentType());
        assertEquals(bytes.length, result.fileSize());

        Path physicalFile = tempStorageDir.resolve(result.storageKey());
        assertTrue(Files.exists(physicalFile));
        assertEquals(content, Files.readString(physicalFile));
    }

    @Test
    void harusBisaMenyimpanFileDocxDenganFallbackContentType() {
        byte[] bytes = "DOCX binary content".getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(bytes);

        ResumeStorageResult result = storageService.store(
                stream,
                "resume.DOCX",
                null // null contentType, should fallback to docx MIME
        );

        assertNotNull(result);
        assertTrue(result.storageKey().endsWith(".docx"));
        assertEquals("resume.DOCX", result.originalFilename());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", result.contentType());
        assertEquals(bytes.length, result.fileSize());
    }

    @Test
    void harusMenghasilkanStorageKeyUnikUntukNamaFileYangSama() {
        byte[] bytes1 = "CV Budi 1".getBytes(StandardCharsets.UTF_8);
        byte[] bytes2 = "CV Budi 2".getBytes(StandardCharsets.UTF_8);

        ResumeStorageResult result1 = storageService.store(
                new ByteArrayInputStream(bytes1),
                "CV.pdf",
                "application/pdf"
        );

        ResumeStorageResult result2 = storageService.store(
                new ByteArrayInputStream(bytes2),
                "CV.pdf",
                "application/pdf"
        );

        assertNotEquals(result1.storageKey(), result2.storageKey());
        assertTrue(Files.exists(tempStorageDir.resolve(result1.storageKey())));
        assertTrue(Files.exists(tempStorageDir.resolve(result2.storageKey())));
    }

    @Test
    void harusBisaMenghapusFileDariStorage() {
        byte[] bytes = "Isi file yang akan dihapus".getBytes(StandardCharsets.UTF_8);
        ResumeStorageResult result = storageService.store(
                new ByteArrayInputStream(bytes),
                "cv_hapus.pdf",
                "application/pdf"
        );

        Path physicalFile = tempStorageDir.resolve(result.storageKey());
        assertTrue(Files.exists(physicalFile));

        storageService.delete(result.storageKey());
        assertFalse(Files.exists(physicalFile));
    }

    @Test
    void harusMenolakFormatFileYangTidakDidukung() {
        byte[] bytes = "dummy executable content".getBytes(StandardCharsets.UTF_8);

        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.store(new ByteArrayInputStream(bytes), "virus.exe", "application/x-msdownload")
        );
        assertTrue(ex1.getMessage().contains("Format file tidak didukung"));

        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.store(new ByteArrayInputStream(bytes), "cv.pdf.exe", "application/octet-stream")
        );
        assertTrue(ex2.getMessage().contains("Format file tidak didukung"));
    }

    @Test
    void harusMenolakFileLebihDari5MB() {
        // Stream buatan yang menghasilkan lebih dari 5 MB
        InputStream largeStream = new InputStream() {
            private long count = 0;
            private final long limit = 5L * 1024 * 1024 + 1024; // 5 MB + 1 KB

            @Override
            public int read() {
                if (count >= limit) {
                    return -1;
                }
                count++;
                return 'A';
            }
        };

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.store(largeStream, "large_cv.pdf", "application/pdf")
        );

        assertTrue(ex.getMessage().contains("5 MB"));
    }

    @Test
    void harusMenghapusPartialFileJikaWriteGagal() throws IOException {
        // InputStream yang melempar IOException di tengah proses pembacaan
        InputStream failingStream = new InputStream() {
            private int bytesRead = 0;

            @Override
            public int read() throws IOException {
                if (bytesRead >= 16) {
                    throw new IOException("Simulasi kegagalan network/disk I/O saat transfer stream");
                }
                bytesRead++;
                return 'X';
            }
        };

        assertThrows(
                IllegalStateException.class,
                () -> storageService.store(failingStream, "partial_cv.pdf", "application/pdf")
        );

        // Pastikan tidak ada file yang tertinggal di direktori storage
        try (var stream = Files.list(tempStorageDir)) {
            List<Path> files = stream.toList();
            assertEquals(0, files.size(), "Direktori storage harus bersih dari partial file");
        }
    }

    @Test
    void harusMencegahPathTraversalSaatDelete() {
        assertThrows(
                SecurityException.class,
                () -> storageService.delete("../../etc/passwd")
        );
    }
}
