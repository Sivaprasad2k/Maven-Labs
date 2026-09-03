package com.shevay.knowledge.generation;

import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.RetrievedChunk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContextAssemblerTest {

    @Test
    @DisplayName("Should return empty string when chunks list is null or empty")
    void testEmptyOrNullChunks() {
        ContextAssembler assembler = new ContextAssembler();

        assertEquals("", assembler.assembleContext(null));
        assertEquals("", assembler.assembleContext(List.of()));
    }

    @Test
    @DisplayName("Should format single chunk into source-aware delimited string")
    void testSingleChunkAssembly() {
        ContextAssembler assembler = new ContextAssembler();
        DocumentChunk chunk = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "Maven is a build automation tool.", 30, 6);
        RetrievedChunk retrieved = new RetrievedChunk(chunk, 0.85);

        String context = assembler.assembleContext(List.of(retrieved));

        assertTrue(context.contains("--- Source [1]: doc1 (Path: knowledge/maven.md, Score: 0.8500) ---"));
        assertTrue(context.contains("Maven is a build automation tool."));
    }

    @Test
    @DisplayName("Should format multiple chunks deterministically in order")
    void testMultipleChunksAssembly() {
        ContextAssembler assembler = new ContextAssembler();
        DocumentChunk chunk1 = new DocumentChunk("c1", "doc1", "knowledge/maven.md", 0, "First chunk text.", 16, 3);
        DocumentChunk chunk2 = new DocumentChunk("c2", "doc2", "knowledge/plugins.md", 1, "Second chunk text.", 17, 3);

        RetrievedChunk r1 = new RetrievedChunk(chunk1, 0.90);
        RetrievedChunk r2 = new RetrievedChunk(chunk2, 0.75);

        String context = assembler.assembleContext(List.of(r1, r2));

        assertTrue(context.contains("--- Source [1]: doc1 (Path: knowledge/maven.md, Score: 0.9000) ---"));
        assertTrue(context.contains("First chunk text."));
        assertTrue(context.contains("--- Source [2]: doc2 (Path: knowledge/plugins.md, Score: 0.7500) ---"));
        assertTrue(context.contains("Second chunk text."));
        assertTrue(context.indexOf("Source [1]") < context.indexOf("Source [2]"));
    }
}
