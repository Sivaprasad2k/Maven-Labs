package com.shevay.knowledge.vector;

import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.VectorRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileVectorStoreTest {

    @TempDir
    Path tempDir;

    private Path storePath;

    @BeforeEach
    void setUp() {
        storePath = tempDir.resolve("vectors.dat");
    }

    @Test
    @DisplayName("Should create binary file and save/load single VectorRecord")
    void testSaveAndLoadSingleRecord() {
        FileVectorStore store = new FileVectorStore(storePath, 3);
        assertTrue(Files.exists(storePath));
        assertEquals(0, store.size());

        float[] vec = new float[]{0.6f, 0.8f, 0.0f};
        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "docs/test.md", 0, "Test chunk content", 18, 4);
        VectorRecord record = new VectorRecord("v1", chunk, vec, 3);

        store.save(record);
        assertEquals(1, store.size());

        Optional<VectorRecord> loadedOpt = store.findById("v1");
        assertTrue(loadedOpt.isPresent());

        VectorRecord loaded = loadedOpt.get();
        assertEquals("v1", loaded.id());
        assertEquals("c1", loaded.chunkId());
        assertEquals("doc1", loaded.documentId());
        assertEquals("docs/test.md", loaded.sourcePath());
        assertEquals(0, loaded.chunkIndex());
        assertEquals("Test chunk content", loaded.text());
        assertEquals(4, loaded.tokenCount());
        assertArrayEquals(vec, loaded.vector(), 1e-6f);
    }

    @Test
    @DisplayName("Should persist records across new FileVectorStore instances (restart survival)")
    void testRestartSurvival() {
        FileVectorStore store1 = new FileVectorStore(storePath, 3);
        DocumentChunk chunk1 = new DocumentChunk("c1", "doc1", "docs/test.md", 0, "Content one", 11, 2);
        DocumentChunk chunk2 = new DocumentChunk("c2", "doc1", "docs/test.md", 1, "Content two", 11, 2);

        store1.save(new VectorRecord("v1", chunk1, new float[]{1.0f, 0.0f, 0.0f}, 3));
        store1.save(new VectorRecord("v2", chunk2, new float[]{0.0f, 1.0f, 0.0f}, 3));
        assertEquals(2, store1.size());

        // Re-instantiate new store pointing to same binary file
        FileVectorStore store2 = new FileVectorStore(storePath, 3);
        assertEquals(2, store2.size());

        Optional<VectorRecord> r1 = store2.findById("v1");
        Optional<VectorRecord> r2 = store2.findById("v2");
        assertTrue(r1.isPresent());
        assertTrue(r2.isPresent());
        assertEquals("Content one", r1.get().text());
        assertEquals("Content two", r2.get().text());

        List<VectorRecord> all = store2.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should handle upsert semantics by appending new record version and updating size/index")
    void testUpsertSemantics() {
        FileVectorStore store = new FileVectorStore(storePath, 3);
        DocumentChunk chunkV1 = new DocumentChunk("c1", "doc1", "docs/test.md", 0, "Original content", 16, 2);
        VectorRecord v1 = new VectorRecord("v1", chunkV1, new float[]{1.0f, 0.0f, 0.0f}, 3);

        store.save(v1);
        assertEquals(1, store.size());
        assertEquals("Original content", store.findById("v1").get().text());

        // Save updated version for same vector ID "v1"
        DocumentChunk chunkV2 = new DocumentChunk("c1", "doc1", "docs/test.md", 0, "Updated content", 15, 2);
        VectorRecord v2 = new VectorRecord("v1", chunkV2, new float[]{0.0f, 1.0f, 0.0f}, 3);

        store.save(v2);
        assertEquals(1, store.size(), "Size should remain 1 after upsert");

        Optional<VectorRecord> updatedOpt = store.findById("v1");
        assertTrue(updatedOpt.isPresent());
        assertEquals("Updated content", updatedOpt.get().text());
        assertArrayEquals(new float[]{0.0f, 1.0f, 0.0f}, updatedOpt.get().vector(), 1e-6f);

        // Verify restart recovers latest upsert version
        FileVectorStore reloadedStore = new FileVectorStore(storePath, 3);
        assertEquals(1, reloadedStore.size());
        assertEquals("Updated content", reloadedStore.findById("v1").get().text());
    }

    @Test
    @DisplayName("Should clear vector store and reset file header")
    void testClearStore() {
        FileVectorStore store = new FileVectorStore(storePath, 3);
        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "docs/test.md", 0, "Content", 7, 1);
        store.save(new VectorRecord("v1", chunk, new float[]{1.0f, 0.0f, 0.0f}, 3));
        assertEquals(1, store.size());

        store.clear();
        assertEquals(0, store.size());
        assertTrue(store.findAll().isEmpty());
        assertTrue(store.findById("v1").isEmpty());

        FileVectorStore reloaded = new FileVectorStore(storePath, 3);
        assertEquals(0, reloaded.size());
    }

    @Test
    @DisplayName("Should save multiple records via saveAll")
    void testSaveAll() {
        FileVectorStore store = new FileVectorStore(storePath, 3);
        List<VectorRecord> records = List.of(
                new VectorRecord("v1", new DocumentChunk("c1", "d1", "p1", 0, "text1", 5, 1), new float[]{1f, 0f, 0f}, 3),
                new VectorRecord("v2", new DocumentChunk("c2", "d1", "p1", 1, "text2", 5, 1), new float[]{0f, 1f, 0f}, 3),
                new VectorRecord("v3", new DocumentChunk("c3", "d1", "p1", 2, "text3", 5, 1), new float[]{0f, 0f, 1f}, 3)
        );

        store.saveAll(records);
        assertEquals(3, store.size());
        assertEquals(3, store.findAll().size());
    }
}
