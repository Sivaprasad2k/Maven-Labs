package com.shevay.knowledge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelsTest {

    @Test
    @DisplayName("Document record should enforce non-null values and immutability")
    void testDocumentInvariants() {
        Document doc = new Document("doc-1", "Test Title", "Content text", "knowledge/java/doc.md", Map.of("author", "Shevay"));
        assertEquals("doc-1", doc.id());
        assertEquals("Test Title", doc.title());
        assertEquals("Content text", doc.content());
        assertEquals("knowledge/java/doc.md", doc.sourcePath());
        assertEquals("Shevay", doc.metadata().get("author"));

        assertThrows(NullPointerException.class, () -> new Document(null, "Title", "Content", "path", Map.of()));
    }

    @Test
    @DisplayName("DocumentChunk record should validate indices and token counts")
    void testDocumentChunkInvariants() {
        DocumentChunk chunk = new DocumentChunk("chunk-1", "doc-1", 0, "Chunk text snippet", 15);
        assertEquals("chunk-1", chunk.id());
        assertEquals("doc-1", chunk.documentId());
        assertEquals(0, chunk.chunkIndex());
        assertEquals("Chunk text snippet", chunk.text());
        assertEquals(15, chunk.tokenCount());

        assertThrows(IllegalArgumentException.class, () -> new DocumentChunk("chunk-1", "doc-1", -1, "Text", 10));
        assertThrows(IllegalArgumentException.class, () -> new DocumentChunk("chunk-1", "doc-1", 0, "Text", -5));
    }

    @Test
    @DisplayName("VectorRecord should clone array defensively and check dimensions")
    void testVectorRecordInvariants() {
        float[] originalVector = new float[]{0.1f, 0.2f, 0.3f};
        VectorRecord vectorRecord = new VectorRecord("vec-1", "chunk-1", originalVector, 3);

        assertEquals("vec-1", vectorRecord.id());
        assertEquals("chunk-1", vectorRecord.chunkId());
        assertEquals(3, vectorRecord.dimensions());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, vectorRecord.vector());

        // Verify array immutability / defensive cloning
        originalVector[0] = 0.9f;
        assertEquals(0.1f, vectorRecord.vector()[0]);

        assertThrows(IllegalArgumentException.class, () -> new VectorRecord("vec-1", "chunk-1", new float[]{0.1f}, 3));
    }

    @Test
    @DisplayName("RetrievedChunk should validate similarity score bounds [-1.0, 1.0]")
    void testRetrievedChunkInvariants() {
        DocumentChunk chunk = new DocumentChunk("chunk-1", "doc-1", 0, "Text", 10);
        RetrievedChunk retrievedChunk = new RetrievedChunk(chunk, 0.85);

        assertEquals(chunk, retrievedChunk.chunk());
        assertEquals(0.85, retrievedChunk.similarityScore());

        assertThrows(IllegalArgumentException.class, () -> new RetrievedChunk(chunk, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new RetrievedChunk(chunk, -1.2));
    }

    @Test
    @DisplayName("SourceReference should enforce non-null values")
    void testSourceReferenceInvariants() {
        SourceReference ref = new SourceReference("doc-1", "knowledge/maven/pom.md", "Snippet text", 0.9);
        assertEquals("doc-1", ref.documentId());
        assertEquals("knowledge/maven/pom.md", ref.sourcePath());
        assertEquals("Snippet text", ref.snippet());
        assertEquals(0.9, ref.relevanceScore());

        assertThrows(NullPointerException.class, () -> new SourceReference(null, "path", "snippet", 0.9));
    }

    @Test
    @DisplayName("RagResponse should handle null collections cleanly as empty immutable lists")
    void testRagResponseInvariants() {
        RagResponse response = new RagResponse("What is Maven?", "Maven is a build tool.", null, null);
        assertEquals("What is Maven?", response.query());
        assertEquals("Maven is a build tool.", response.generatedAnswer());
        assertNotNull(response.retrievedChunks());
        assertTrue(response.retrievedChunks().isEmpty());
        assertNotNull(response.sources());
        assertTrue(response.sources().isEmpty());
    }
}
