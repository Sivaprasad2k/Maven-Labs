package com.shevay.knowledge.vector;

import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.VectorRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VectorStoreCorruptionTest {

    @TempDir
    Path tempDir;

    private Path storePath;

    @BeforeEach
    void setUp() {
        storePath = tempDir.resolve("corrupt_vectors.dat");
    }

    @Test
    @DisplayName("Should throw VectorStoreException when file header is truncated")
    void testTruncatedHeader() throws IOException {
        Files.write(storePath, new byte[]{0x4D, 0x4B, 0x4C}); // 3 bytes < 12
        assertThrows(VectorStoreException.class, () -> new FileVectorStore(storePath, 768));
    }

    @Test
    @DisplayName("Should throw VectorStoreException when magic bytes are invalid")
    void testInvalidMagicBytes() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storePath.toFile()))) {
            dos.writeInt(0xDEADBEEF); // Bad magic
            dos.writeInt(1);
            dos.writeInt(768);
        }

        VectorStoreException ex = assertThrows(VectorStoreException.class, () -> new FileVectorStore(storePath, 768));
        assertTrue(ex.getMessage().contains("Invalid vector store magic"));
    }

    @Test
    @DisplayName("Should throw VectorStoreException when format version is unsupported")
    void testUnsupportedFormatVersion() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storePath.toFile()))) {
            dos.writeInt(FileVectorStore.MAGIC_BYTES);
            dos.writeInt(99); // Unsupported version 99
            dos.writeInt(768);
        }

        VectorStoreException ex = assertThrows(VectorStoreException.class, () -> new FileVectorStore(storePath, 768));
        assertTrue(ex.getMessage().contains("Unsupported format version 99"));
    }

    @Test
    @DisplayName("Should throw VectorStoreException when file dimensions do not match expected dimensions")
    void testDimensionMismatch() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storePath.toFile()))) {
            dos.writeInt(FileVectorStore.MAGIC_BYTES);
            dos.writeInt(1);
            dos.writeInt(384); // Stored 384 vs Expected 768
        }

        VectorStoreException ex = assertThrows(VectorStoreException.class, () -> new FileVectorStore(storePath, 768));
        assertTrue(ex.getMessage().contains("384"));
        assertTrue(ex.getMessage().contains("768"));
    }

    @Test
    @DisplayName("Should throw VectorStoreException when saving a record with mismatched dimensions")
    void testRecordDimensionMismatchOnSave() {
        FileVectorStore store = new FileVectorStore(storePath, 768);
        DocumentChunk chunk = new DocumentChunk("c1", "d1", "p1", 0, "text", 4, 1);
        VectorRecord badRecord = new VectorRecord("v1", chunk, new float[]{1.0f, 2.0f, 3.0f}, 3);

        assertThrows(VectorStoreException.class, () -> store.save(badRecord));
    }

    @Test
    @DisplayName("Should throw VectorStoreException when record payload is truncated or corrupted")
    void testCorruptRecordPayload() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storePath.toFile()))) {
            dos.writeInt(FileVectorStore.MAGIC_BYTES);
            dos.writeInt(1);
            dos.writeInt(3);

            // Record with payload length claiming 100 bytes, but file ends prematurely
            dos.writeInt(100);
            dos.writeUTF("v1");
        }

        assertThrows(VectorStoreException.class, () -> new FileVectorStore(storePath, 3));
    }
}
