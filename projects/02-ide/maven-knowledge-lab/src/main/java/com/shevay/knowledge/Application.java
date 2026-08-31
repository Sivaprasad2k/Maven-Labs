package com.shevay.knowledge;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentIngestionService;
import com.shevay.knowledge.document.IngestionResult;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingException;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.embedding.GeminiEmbeddingProvider;

import java.io.IOException;

/**
 * Main application entry point for Maven Knowledge Lab.
 * Supports Stage 1 initialization, Stage 2 ingest, and Stage 3 embed CLI sub-commands.
 */
public class Application {

    public static void main(String[] args) {
        AppConfig config = AppConfig.loadDefaults();

        if (args != null && args.length > 0) {
            String command = args[0].toLowerCase();
            if ("ingest".equals(command)) {
                runIngestCommand(config);
                return;
            } else if ("embed".equals(command)) {
                String inputText = (args.length > 1 && !args[1].isBlank())
                        ? args[1]
                        : "What is the Maven lifecycle?";
                runEmbedCommand(config, inputText);
                return;
            }
        }
        runDefaultStartup(config);
    }

    private static void runEmbedCommand(AppConfig config, String text) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Stage 3 Embedding Provider Execution");
        System.out.println();
        System.out.println("Target text : \"" + text + "\"");
        System.out.println("--------------------------------------------------");

        try {
            EmbeddingProvider provider = new GeminiEmbeddingProvider(config);
            Embedding embedding = provider.embed(text, EmbeddingPurpose.QUERY);

            float[] values = embedding.getValues();
            String snippet = values.length >= 3
                    ? String.format("[%f, %f, %f...]", values[0], values[1], values[2])
                    : "[]";

            System.out.println("Provider   : " + config.getEmbeddingProvider());
            System.out.println("Model      : " + embedding.getModelIdentifier());
            System.out.println("Purpose    : QUERY");
            System.out.println("Dimensions : " + embedding.getDimensions());
            System.out.println("Normalized : true");
            System.out.println("Vector Head: " + snippet);
            System.out.println("--------------------------------------------------");
            System.out.println("Embedding generated successfully.");
        } catch (IllegalStateException e) {
            System.err.println("Configuration Error: " + e.getMessage());
            System.err.println("Please set the GEMINI_API_KEY environment variable before running embed command.");
            System.exit(1);
        } catch (EmbeddingException e) {
            System.err.println("Embedding Failed: " + e.getMessage());
            System.exit(1);
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
        System.out.println(" - Embedding Provider   : " + config.getEmbeddingProvider());
        System.out.println(" - Embedding Model      : " + config.getEmbeddingModel());
        System.out.println(" - Embedding Dimensions : " + config.getEmbeddingDimensions());
        System.out.println(" - Retrieval Top-K      : " + config.getTopK());
        System.out.println(" - Min Similarity Score : " + config.getMinSimilarity());
        System.out.println("--------------------------------------------------");
        System.out.println("Stage 1, 2 & 3 active. RAG/AI vector storage offline.");
        System.out.println("Use command 'ingest' to execute document ingestion.");
        System.out.println("Use command 'embed <text>' to execute vector embedding generation.");
        System.out.println("Application completed execution cleanly.");
    }
}
