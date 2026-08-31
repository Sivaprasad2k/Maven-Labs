package com.shevay.knowledge.document;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.model.Document;
import com.shevay.knowledge.model.DocumentChunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Orchestrating service for Stage 2 document ingestion and chunking pipeline.
 */
public class DocumentIngestionService {

    private final DocumentLoader loader;
    private final TextChunker chunker;

    public DocumentIngestionService(AppConfig config) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.loader = new DocumentLoader();
        this.chunker = new TextChunker(config);
    }

    public DocumentIngestionService(DocumentLoader loader, TextChunker chunker) {
        this.loader = Objects.requireNonNull(loader, "DocumentLoader must not be null");
        this.chunker = Objects.requireNonNull(chunker, "TextChunker must not be null");
    }

    /**
     * Ingests documents from the given knowledge path string.
     *
     * @param knowledgePathStr target directory path string
     * @return IngestionResult containing metrics and chunks
     * @throws IOException if an I/O error occurs
     */
    public IngestionResult ingest(String knowledgePathStr) throws IOException {
        if (knowledgePathStr == null || knowledgePathStr.isBlank()) {
            throw new IllegalArgumentException("Knowledge path must not be null or blank");
        }
        return ingest(Paths.get(knowledgePathStr));
    }

    /**
     * Ingests documents from the given knowledge directory Path.
     *
     * @param knowledgeDir target directory Path
     * @return IngestionResult containing metrics and chunks
     * @throws IOException if an I/O error occurs
     */
    public IngestionResult ingest(Path knowledgeDir) throws IOException {
        if (knowledgeDir == null || !Files.exists(knowledgeDir)) {
            throw new IllegalArgumentException("Knowledge path does not exist: " + knowledgeDir);
        }
        if (!Files.isDirectory(knowledgeDir)) {
            throw new IllegalArgumentException("Knowledge path is not a directory: " + knowledgeDir);
        }

        Path normalizedRoot = knowledgeDir.toAbsolutePath().normalize();
        int totalDiscovered = 0;
        int skippedCount = 0;

        try (Stream<Path> stream = Files.walk(normalizedRoot)) {
            List<Path> allFiles = stream.filter(Files::isRegularFile).toList();
            totalDiscovered = allFiles.size();
            for (Path file : allFiles) {
                String fileName = file.getFileName().toString();
                if (!DocumentLoader.isSupportedExtension(fileName) || Files.readString(file).isBlank()) {
                    skippedCount++;
                }
            }
        }

        List<Document> loadedDocs = loader.loadDocuments(normalizedRoot);
        List<DocumentChunk> allChunks = new ArrayList<>();

        for (Document doc : loadedDocs) {
            List<DocumentChunk> docChunks = chunker.chunkDocument(doc);
            allChunks.addAll(docChunks);
        }

        return new IngestionResult(
                totalDiscovered,
                loadedDocs.size(),
                allChunks.size(),
                skippedCount,
                0,
                loadedDocs,
                allChunks
        );
    }
}
