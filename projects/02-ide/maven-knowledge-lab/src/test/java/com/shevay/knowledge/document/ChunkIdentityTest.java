package com.shevay.knowledge.document;

import com.shevay.knowledge.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ChunkIdentityTest {

    @Test
    @DisplayName("Same document ID, chunk index, and text should produce identical chunk ID")
    void testIdenticalChunkId() {
        String docId = "doc-abc";
        int chunkIndex = 0;
        String text = "Chunk text snippet.";

        String chunkId1 = HashUtil.sha256(docId + ":" + chunkIndex + ":" + text);
        String chunkId2 = HashUtil.sha256(docId + ":" + chunkIndex + ":" + text);

        assertEquals(chunkId1, chunkId2);
    }

    @Test
    @DisplayName("Changing chunk index or content should produce different chunk ID")
    void testDifferentChunkId() {
        String docId = "doc-abc";
        String text = "Chunk text snippet.";

        String chunkId0 = HashUtil.sha256(docId + ":0:" + text);
        String chunkId1 = HashUtil.sha256(docId + ":1:" + text);
        String chunkIdMod = HashUtil.sha256(docId + ":0:" + text + " modified");

        assertNotEquals(chunkId0, chunkId1);
        assertNotEquals(chunkId0, chunkIdMod);
    }
}
