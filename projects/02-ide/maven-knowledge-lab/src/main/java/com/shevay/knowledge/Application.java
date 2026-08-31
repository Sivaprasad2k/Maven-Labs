package com.shevay.knowledge;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentIngestionService;
import com.shevay.knowledge.document.IngestionResult;

import java.io.IOException;

/**
 * Main application entry point for Maven Knowledge Lab.
 * Supports Stage 1 initialization and Stage 2 ingest CLI command.
 */
public class Application {

    public static void main(String[] args) {
        AppConfig config = AppConfig.loadDefaults();

        if (args != null && args.length > 0 && "ingest".equalsIgnoreCase(args[0])) {
            runIngestCommand(config);
        } else {
            runDefaultStartup(config);
        }
    }

    private static void runIngestCommand(AppConfig config) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Stage 2 Document Ingestion & Deterministic Chunking");
        System.out.println();
        System.out.println("Knowledge path: " + config.getKnowledgePath());
        System.out.println("Chunk size: " + config.getChunkSize() + " | Overlap: " + config.getChunkOverlap());
        System.out.println("--------------------------------------------------");

        try {
            DocumentIngestionService ingestionService = new DocumentIngestionService(config);
            IngestionResult result = ingestionService.ingest(config.getKnowledgePath());

            System.out.println("Documents discovered : " + result.documentsDiscovered());
            System.out.println("Documents processed  : " + result.documentsProcessed());
            System.out.println("Chunks created       : " + result.chunksCreated());
            System.out.println("Skipped files        : " + result.skippedFiles());
            System.out.println("Failures             : " + result.failures());
            System.out.println("--------------------------------------------------");
            System.out.println("Ingestion completed successfully.");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Ingestion failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runDefaultStartup(AppConfig config) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");

        System.out.println("Configuration loaded successfully:");
        System.out.println(" - Knowledge Path       : " + config.getKnowledgePath());
        System.out.println(" - Data Path            : " + config.getDataPath());
        System.out.println(" - Chunk Size           : " + config.getChunkSize());
        System.out.println(" - Chunk Overlap        : " + config.getChunkOverlap());
        System.out.println(" - Retrieval Top-K      : " + config.getTopK());
        System.out.println(" - Min Similarity Score : " + config.getMinSimilarity());
        System.out.println("--------------------------------------------------");
        System.out.println("Stage 1 & Stage 2 active. RAG/AI components offline.");
        System.out.println("Use command 'ingest' to execute document ingestion.");
        System.out.println("Application completed execution cleanly.");
    }
}
