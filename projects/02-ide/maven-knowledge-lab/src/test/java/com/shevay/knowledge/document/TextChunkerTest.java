package com.shevay.knowledge.document;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.model.Document;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {

    @Test
    @DisplayName("Should create single chunk when content is smaller than chunk size")
    void testContentSmallerThanChunkSize() {
        TextChunker chunker = new TextChunker(100, 20);
        Document doc = new Document("doc-1", "Title", "Short content text.", "path/doc.md", Map.of());

        List<DocumentChunk> chunks = chunker.chunkDocument(doc);
        assertEquals(1, chunks.size());
        assertEquals("Short content text.", chunks.get(0).text());
        assertEquals(0, chunks.get(0).chunkIndex());
    }

    @Test
    @DisplayName("Should create multiple overlapping chunks when content exceeds chunk size")
    void testContentLargerThanChunkSize() {
        TextChunker chunker = new TextChunker(50, 10);
        String text = "Paragraph one with some text. Paragraph two with more technical text. Paragraph three completes the content.";
        Document doc = new Document("doc-1", "Title", text, "path/doc.md", Map.of());

        List<DocumentChunk> chunks = chunker.chunkDocument(doc);
        assertTrue(chunks.size() > 1);

        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).chunkIndex());
            assertEquals("doc-1", chunks.get(i).documentId());
            assertNotNull(chunks.get(i).id());
        }
    }

    @Test
    @DisplayName("Should validate chunk size and overlap configuration bounds")
    void testConfigurationValidation() {
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(-50, 10));
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(100, -5));
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(100, 100));
        assertThrows(IllegalArgumentException.class, () -> new TextChunker(100, 150));
    }

    @Test
    @DisplayName("Should handle empty document content cleanly")
    void testEmptyContent() {
        TextChunker chunker = new TextChunker(100, 20);
        Document doc = new Document("doc-1", "Title", "", "path/doc.md", Map.of());

        List<DocumentChunk> chunks = chunker.chunkDocument(doc);
        assertTrue(chunks.isEmpty());
    }

    @Test
    @DisplayName("Should produce deterministic chunk IDs and contents across multiple calls")
    void testDeterminism() {
        TextChunker chunker = new TextChunker(800, 100);
        Document doc = new Document("doc-1", "Title", "Deterministic test content for chunking verification.", "path/doc.md", Map.of());

        List<DocumentChunk> pass1 = chunker.chunkDocument(doc);
        List<DocumentChunk> pass2 = chunker.chunkDocument(doc);

        assertEquals(pass1.size(), pass2.size());
        for (int i = 0; i < pass1.size(); i++) {
            assertEquals(pass1.get(i).id(), pass2.get(i).id());
            assertEquals(pass1.get(i).text(), pass2.get(i).text());
        }
    }
}
