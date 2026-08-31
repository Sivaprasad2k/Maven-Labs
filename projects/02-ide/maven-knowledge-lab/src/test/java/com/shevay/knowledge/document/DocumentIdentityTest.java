package com.shevay.knowledge.document;

import com.shevay.knowledge.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DocumentIdentityTest {

    @Test
    @DisplayName("Same relative path should produce identical document ID regardless of machine path")
    void testSameRelativePathSameId() {
        String windowsPath = "knowledge\\java\\collections.md";
        String unixPath = "knowledge/java/collections.md";

        String normWin = HashUtil.normalizePath(windowsPath);
        String normUnix = HashUtil.normalizePath(unixPath);

        assertEquals("knowledge/java/collections.md", normWin);
        assertEquals("knowledge/java/collections.md", normUnix);

        String id1 = HashUtil.sha256(normWin);
        String id2 = HashUtil.sha256(normUnix);

        assertEquals(id1, id2);
    }

    @Test
    @DisplayName("Different relative paths should produce different document IDs")
    void testDifferentRelativePathDifferentId() {
        String id1 = HashUtil.sha256(HashUtil.normalizePath("java/collections.md"));
        String id2 = HashUtil.sha256(HashUtil.normalizePath("java/concurrency.md"));

        assertNotEquals(id1, id2);
    }
}
