package com.mushroom.expense.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void storeFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());

        String fileName = fileStorageService.storeFile(file);

        assertNotNull(fileName);
        assertTrue(fileName.contains("test.txt"));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    void storeFile_InvalidFileName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test..txt",
                "text/plain",
                "Hello, World!".getBytes());

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void deleteFile_Success() throws IOException {
        Path filePath = tempDir.resolve("test-delete.txt");
        Files.createFile(filePath);

        fileStorageService.deleteFile("test-delete.txt");

        assertFalse(Files.exists(filePath));
    }

    @Test
    void storeFile_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);

        // Depending on implementation, this might throw or return null/empty.
        // Assuming current implementation doesn't explicitly check for empty content
        // but standard copy handles it.
        // If we want to enforce non-empty, we'd need to update service.
        // For now, let's just verify it doesn't crash.
        assertDoesNotThrow(() -> fileStorageService.storeFile(file));
    }

    @Test
    void storeFile_NoOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "text/plain",
                "content".getBytes());

        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file));
    }
}
