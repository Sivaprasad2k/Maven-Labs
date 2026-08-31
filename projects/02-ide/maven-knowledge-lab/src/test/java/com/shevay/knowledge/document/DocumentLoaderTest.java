package com.shevay.knowledge.document;

import com.shevay.knowledge.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentLoaderTest {

    @Test
    @DisplayName("Should recursively discover .md and .txt files and filter unsupported extensions")
    void testRecursiveDiscoveryAndFiltering(@TempDir Path tempDir) throws IOException {
        Path javaDir = tempDir.resolve("java");
        Files.createDirectories(javaDir);

        Path doc1 = javaDir.resolve("collections.md");
        Files.writeString(doc1, "# Java Collections\nLists and Sets", StandardCharsets.UTF_8);

        Path doc2 = tempDir.resolve("notes.txt");
        Files.writeString(doc2, "General notes text", StandardCharsets.UTF_8);

        Path unsupported = tempDir.resolve("image.png");
        Files.writeString(unsupported, "binary content", StandardCharsets.UTF_8);

        DocumentLoader loader = new DocumentLoader();
        List<Document> documents = loader.loadDocuments(tempDir);

        assertEquals(2, documents.size());
        assertTrue(documents.stream().anyMatch(d -> d.sourcePath().equals("java/collections.md")));
        assertTrue(documents.stream().anyMatch(d -> d.sourcePath().equals("notes.txt")));
    }

    @Test
    @DisplayName("Should preserve relative paths and extract Markdown titles")
    void testTitleAndRelativePath(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("maven");
        Files.createDirectories(subDir);
        Path doc = subDir.resolve("pom.md");
        Files.writeString(doc, "# Maven POM Guide\nContent here", StandardCharsets.UTF_8);

        DocumentLoader loader = new DocumentLoader();
        List<Document> documents = loader.loadDocuments(tempDir);

        assertEquals(1, documents.size());
        Document document = documents.get(0);
        assertEquals("maven/pom.md", document.sourcePath());
        assertEquals("Maven POM Guide", document.title());
    }

    @Test
    @DisplayName("Should skip empty files according to empty file policy")
    void testEmptyFileSkipping(@TempDir Path tempDir) throws IOException {
        Path emptyDoc = tempDir.resolve("empty.md");
        Files.writeString(emptyDoc, "   \n\t ", StandardCharsets.UTF_8);

        DocumentLoader loader = new DocumentLoader();
        List<Document> documents = loader.loadDocuments(tempDir);

        assertTrue(documents.isEmpty());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when root directory does not exist")
    void testMissingDirectory() {
        DocumentLoader loader = new DocumentLoader();
        assertThrows(IllegalArgumentException.class, () ->
                loader.loadDocuments(Path.of("non_existent_directory_12345"))
        );
    }
}
