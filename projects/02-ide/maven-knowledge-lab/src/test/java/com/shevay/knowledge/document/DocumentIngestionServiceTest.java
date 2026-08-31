package com.shevay.knowledge.document;

import com.shevay.knowledge.config.AppConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentIngestionServiceTest {

    @Test
    @DisplayName("Should execute full ingestion pipeline and return accurate IngestionResult")
    void testFullIngestionPipeline(@TempDir Path tempDir) throws IOException {
        Path javaDir = tempDir.resolve("java");
        Files.createDirectories(javaDir);

        Path file1 = javaDir.resolve("doc1.md");
        Files.writeString(file1, "# Java Document 1\nSome introductory content for testing.", StandardCharsets.UTF_8);

        Path file2 = tempDir.resolve("doc2.txt");
        Files.writeString(file2, "Plain text document content for testing.", StandardCharsets.UTF_8);

        Path unsupported = tempDir.resolve("binary.dat");
        Files.writeString(unsupported, "data", StandardCharsets.UTF_8);

        AppConfig config = new AppConfig(tempDir.toString(), "data", 3, 0.7, 50, 10);
        DocumentIngestionService service = new DocumentIngestionService(config);

        IngestionResult result = service.ingest(tempDir);

        assertEquals(3, result.documentsDiscovered());
        assertEquals(2, result.documentsProcessed());
        assertTrue(result.chunksCreated() >= 2);
        assertEquals(1, result.skippedFiles());
        assertEquals(0, result.failures());
        assertEquals(2, result.documents().size());
    }

    @Test
    @DisplayName("Should fail clearly when knowledge directory does not exist")
    void testNonExistentDirectory() {
        AppConfig config = AppConfig.loadDefaults();
        DocumentIngestionService service = new DocumentIngestionService(config);

        assertThrows(IllegalArgumentException.class, () ->
                service.ingest(Path.of("invalid_dir_path_xyz"))
        );
    }
}
