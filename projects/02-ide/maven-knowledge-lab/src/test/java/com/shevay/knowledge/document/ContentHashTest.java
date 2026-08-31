package com.shevay.knowledge.document;

import com.shevay.knowledge.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ContentHashTest {

    @Test
    @DisplayName("Identical content should produce identical SHA-256 content hash")
    void testIdenticalContentHash() {
        String content = "# Title\nThis is test content.";
        String hash1 = HashUtil.sha256(content);
        String hash2 = HashUtil.sha256(content);

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Modifying content slightly should produce different content hash")
    void testModifiedContentHash() {
        String content1 = "# Title\nThis is test content.";
        String content2 = "# Title\nThis is test content!";

        String hash1 = HashUtil.sha256(content1);
        String hash2 = HashUtil.sha256(content2);

        assertNotEquals(hash1, hash2);
    }
}
