package com.shevay.knowledge;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentIngestionService;
import com.shevay.knowledge.document.IngestionResult;
import com.shevay.knowledge.embedding.DummyEmbeddingProvider;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingException;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.embedding.GeminiEmbeddingProvider;
import com.shevay.knowledge.model.DocumentChunk;
import com.shevay.knowledge.model.RetrievedChunk;
import com.shevay.knowledge.model.VectorRecord;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;
import com.shevay.knowledge.vector.VectorStoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application entry point for Maven Knowledge Lab.
 * Supports Stage 1 initialization, Stage 2 ingest, Stage 3 embed, and Phase 4 index/search CLI sub-commands.
 */
public class Application {

    public static void main(String[] args) {
        AppConfig config = AppConfig.loadDefaults();

        if (args != null && args.length > 0) {
            String command = args[0].toLowerCase();
            switch (command) {
                case "ingest" -> {
                    runIngestCommand(config);
                    return;
                }
                case "embed" -> {
                    String inputText = (args.length > 1 && !args[1].isBlank())
                            ? args[1]
                            : "What is the Maven lifecycle?";
                    runEmbedCommand(config, inputText);
                    return;
                }
                case "index" -> {
                    runIndexCommand(config);
                    return;
                }
                case "search" -> {
                    String queryText = (args.length > 1 && !args[1].isBlank())
                            ? args[1]
                            : "What is the Maven build lifecycle?";
                    runSearchCommand(config, queryText);
                    return;
                }
                case "rag" -> {
                    String queryText = (args.length > 1 && !args[1].isBlank())
                            ? args[1]
                            : "What is the Maven build lifecycle?";
                    runRagCommand(config, queryText);
                    return;
                }
                case "agent" -> {
                    String queryText = (args.length > 1 && !args[1].isBlank())
                            ? args[1]
                            : "What is the Maven build lifecycle?";
                    runAgentCommand(config, queryText);
                    return;
                }
            }
        }
        runDefaultStartup(config);
    }

    private static EmbeddingProvider createEmbeddingProvider(AppConfig config) {
        String providerName = config.getEmbeddingProvider();
        if ("dummy".equalsIgnoreCase(providerName) || System.getProperty("embedding.provider", "").equalsIgnoreCase("dummy")) {
            System.out.println("[Notice] Using DummyEmbeddingProvider for offline vector operations.");
            return new DummyEmbeddingProvider(config.getEmbeddingDimensions());
        }
        String apiKey = config.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[Notice] GEMINI_API_KEY environment variable not set. Falling back to DummyEmbeddingProvider for offline testing.");
            return new DummyEmbeddingProvider(config.getEmbeddingDimensions());
        }
        return new GeminiEmbeddingProvider(config);
    }

    private static com.shevay.knowledge.generation.LlmGenerationProvider createLlmGenerationProvider(AppConfig config) {
        String providerName = config.getGenerationProvider();
        if ("dummy".equalsIgnoreCase(providerName) || System.getProperty("generation.provider", "").equalsIgnoreCase("dummy")) {
            System.out.println("[Notice] Using DummyLlmGenerationProvider for offline text generation.");
            return new com.shevay.knowledge.generation.DummyLlmGenerationProvider();
        }
        String apiKey = config.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[Notice] GEMINI_API_KEY environment variable not set. Falling back to DummyLlmGenerationProvider for offline testing.");
            return new com.shevay.knowledge.generation.DummyLlmGenerationProvider();
        }
        return new com.shevay.knowledge.generation.GeminiGenerationProvider(config);
    }

    private static void runRagCommand(AppConfig config, String queryText) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Phase 5 RAG Answer Generation Execution");
        System.out.println();
        System.out.println("Query Text       : \"" + queryText + "\"");
        System.out.println("Vector Store Path: " + config.getVectorStorePath());
        System.out.println("Top-K Limit      : " + config.getTopK());
        System.out.println("Min Similarity   : " + config.getMinSimilarity());
        System.out.println("Generation Model : " + config.getGenerationModel());
        System.out.println("--------------------------------------------------");

        try {
            VectorStore vectorStore = new FileVectorStore(config);
            if (vectorStore.size() == 0) {
                System.out.println("Vector store at '" + config.getVectorStorePath() + "' is empty.");
                System.out.println("Please run 'java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar index' first to populate vectors.");
                return;
            }

            EmbeddingProvider embeddingProvider = createEmbeddingProvider(config);
            com.shevay.knowledge.generation.LlmGenerationProvider generationProvider = createLlmGenerationProvider(config);

            com.shevay.knowledge.generation.RagService ragService = new com.shevay.knowledge.generation.RagService(config, embeddingProvider, generationProvider);
            com.shevay.knowledge.model.RagResponse ragResponse = ragService.query(queryText, vectorStore);

            System.out.println("Qualifying Chunks Found : " + ragResponse.retrievedChunks().size());
            System.out.println("Source References       : " + ragResponse.sources().size());
            System.out.println("--------------------------------------------------");
            System.out.println("GENERATED ANSWER:");
            System.out.println(ragResponse.generatedAnswer());
            System.out.println("--------------------------------------------------");
            if (!ragResponse.sources().isEmpty()) {
                System.out.println("SOURCES:");
                for (int i = 0; i < ragResponse.sources().size(); i++) {
                    com.shevay.knowledge.model.SourceReference src = ragResponse.sources().get(i);
                    System.out.printf("[%d] %s (Path: %s, Score: %.4f)%n",
                            i + 1, src.documentId(), src.sourcePath(), src.relevanceScore());
                }
                System.out.println("--------------------------------------------------");
            }
            System.out.println("RAG answer generation completed successfully.");
        } catch (VectorStoreException | EmbeddingException | com.shevay.knowledge.generation.GenerationException | IllegalArgumentException e) {
            System.err.println("RAG Execution Failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runIndexCommand(AppConfig config) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Phase 4 Vector Indexing Execution");
        System.out.println();
        System.out.println("Knowledge Path   : " + config.getKnowledgePath());
        System.out.println("Vector Store Path: " + config.getVectorStorePath());
        System.out.println("--------------------------------------------------");

        try {
            DocumentIngestionService ingestionService = new DocumentIngestionService(config);
            IngestionResult ingestionResult = ingestionService.ingest(config.getKnowledgePath());

            List<DocumentChunk> chunks = ingestionResult.chunks();
            if (chunks.isEmpty()) {
                System.out.println("No chunks generated during ingestion. Indexing aborted.");
                return;
            }

            EmbeddingProvider embeddingProvider = createEmbeddingProvider(config);
            VectorStore vectorStore = new FileVectorStore(config);

            List<VectorRecord> recordsToSave = new ArrayList<>(chunks.size());
            for (DocumentChunk chunk : chunks) {
                Embedding embedding = embeddingProvider.embed(chunk.text(), EmbeddingPurpose.DOCUMENT);
                VectorRecord record = new VectorRecord("vec-" + chunk.id(), chunk, embedding.getValues(), embedding.getDimensions());
                recordsToSave.add(record);
            }

            vectorStore.saveAll(recordsToSave);

            System.out.println("Documents Ingested : " + ingestionResult.documentsProcessed());
            System.out.println("Chunks Created     : " + chunks.size());
            System.out.println("Vectors Indexed    : " + recordsToSave.size());
            System.out.println("Total Store Records: " + vectorStore.size());
            System.out.println("--------------------------------------------------");
            System.out.println("Vector indexing completed successfully.");
        } catch (IOException | VectorStoreException | EmbeddingException e) {
            System.err.println("Indexing Failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runSearchCommand(AppConfig config, String queryText) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Phase 4 Exact Similarity Search Execution");
        System.out.println();
        System.out.println("Query Text       : \"" + queryText + "\"");
        System.out.println("Vector Store Path: " + config.getVectorStorePath());
        System.out.println("Top-K Limit      : " + config.getTopK());
        System.out.println("Min Similarity   : " + config.getMinSimilarity());
        System.out.println("--------------------------------------------------");

        try {
            VectorStore vectorStore = new FileVectorStore(config);
            if (vectorStore.size() == 0) {
                System.out.println("Vector store at '" + config.getVectorStorePath() + "' is empty.");
                System.out.println("Please run 'java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar index' first to populate vectors.");
                return;
            }

            EmbeddingProvider embeddingProvider = createEmbeddingProvider(config);
            Embedding queryEmbedding = embeddingProvider.embed(queryText, EmbeddingPurpose.QUERY);

            SimilaritySearchService searchService = new SimilaritySearchService(config);
            List<RetrievedChunk> results = searchService.search(queryEmbedding, vectorStore);

            System.out.println("Stored Vectors Searched : " + vectorStore.size());
            System.out.println("Qualifying Chunks Found : " + results.size());
            System.out.println("--------------------------------------------------");

            if (results.isEmpty()) {
                System.out.println("No chunks met the minimum similarity score threshold (" + config.getMinSimilarity() + ").");
            } else {
                for (int i = 0; i < results.size(); i++) {
                    RetrievedChunk retrieved = results.get(i);
                    DocumentChunk chunk = retrieved.chunk();
                    String textSnippet = chunk.text().strip();
                    if (textSnippet.length() > 120) {
                        textSnippet = textSnippet.substring(0, 117) + "...";
                    }
                    textSnippet = textSnippet.replace("\n", " ").replace("\r", "");

                    System.out.printf("[Rank %d] Similarity Score: %.4f%n", (i + 1), retrieved.similarityScore());
                    System.out.println("         Chunk ID  : " + chunk.id());
                    System.out.println("         Source    : " + chunk.sourcePath() + " (Chunk #" + chunk.chunkIndex() + ")");
                    System.out.println("         Snippet   : \"" + textSnippet + "\"");
                    System.out.println("--------------------------------------------------");
                }
            }
            System.out.println("Search completed successfully.");
        } catch (VectorStoreException | EmbeddingException | IllegalArgumentException e) {
            System.err.println("Search Failed: " + e.getMessage());
            System.exit(1);
        }
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
            EmbeddingProvider provider = createEmbeddingProvider(config);
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

    private static void runAgentCommand(AppConfig config, String queryText) {
        System.out.println("==================================================");
        System.out.println("Maven Knowledge Lab - CLI RAG System");
        System.out.println("==================================================");
        System.out.println("Phase 7 Controlled Knowledge Agent Execution");
        System.out.println();
        System.out.println("Query Text       : \"" + queryText + "\"");
        System.out.println("Vector Store Path: " + config.getVectorStorePath());
        System.out.println("Max Iterations   : " + com.shevay.knowledge.agent.KnowledgeAgent.DEFAULT_MAX_ITERATIONS);
        System.out.println("--------------------------------------------------");

        try {
            VectorStore vectorStore = new FileVectorStore(config);
            EmbeddingProvider embeddingProvider = createEmbeddingProvider(config);
            SimilaritySearchService searchService = new SimilaritySearchService(config);
            com.shevay.knowledge.generation.LlmGenerationProvider generationProvider = createLlmGenerationProvider(config);
            com.shevay.knowledge.generation.RagService ragService = new com.shevay.knowledge.generation.RagService(config, embeddingProvider, generationProvider);

            com.shevay.knowledge.agent.ToolRegistry toolRegistry = new com.shevay.knowledge.agent.ToolRegistry();
            toolRegistry.register(new com.shevay.knowledge.agent.tools.SearchKnowledgeTool(searchService, embeddingProvider, vectorStore));
            toolRegistry.register(new com.shevay.knowledge.agent.tools.GetDocumentTool(config));
            toolRegistry.register(new com.shevay.knowledge.agent.tools.ExplainMavenConceptTool(ragService, vectorStore));

            com.shevay.knowledge.agent.AgentDecisionProvider decisionProvider;
            String apiKey = config.getGeminiApiKey();
            if (apiKey != null && !apiKey.isBlank()) {
                decisionProvider = new com.shevay.knowledge.agent.GeminiAgentDecisionProvider(config);
            } else {
                System.out.println("[Notice] GEMINI_API_KEY environment variable not set. Falling back to DummyAgentDecisionProvider for offline execution.");
                decisionProvider = new com.shevay.knowledge.agent.DummyAgentDecisionProvider(
                        "Grounded Answer: Maven lifecycle consists of validate, compile, test, package, verify, install, and deploy phases."
                );
            }

            com.shevay.knowledge.agent.KnowledgeAgent agent = new com.shevay.knowledge.agent.KnowledgeAgent(toolRegistry, decisionProvider);

            System.out.println("Registered Tools : " + toolRegistry.getTools().size());
            System.out.println("--------------------------------------------------");
            System.out.println("AGENT ANSWER:");
            String answer = agent.execute(queryText);
            System.out.println(answer);
            System.out.println("--------------------------------------------------");
            System.out.println("Controlled agent execution completed successfully.");
        } catch (Exception e) {
            System.err.println("Agent Execution Failed: " + e.getMessage());
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
        System.out.println(" - Vector Store Path    : " + config.getVectorStorePath());
        System.out.println(" - Chunk Size           : " + config.getChunkSize());
        System.out.println(" - Chunk Overlap        : " + config.getChunkOverlap());
        System.out.println(" - Embedding Provider   : " + config.getEmbeddingProvider());
        System.out.println(" - Embedding Model      : " + config.getEmbeddingModel());
        System.out.println(" - Embedding Dimensions : " + config.getEmbeddingDimensions());
        System.out.println(" - Generation Provider  : " + config.getGenerationProvider());
        System.out.println(" - Generation Model     : " + config.getGenerationModel());
        System.out.println(" - Retrieval Top-K      : " + config.getTopK());
        System.out.println(" - Min Similarity Score : " + config.getMinSimilarity());
        System.out.println("--------------------------------------------------");
        System.out.println("Phases 1, 2, 3, 4, 5, 6 & 7 Active.");
        System.out.println("Available commands:");
        System.out.println("  ingest        - Run document ingestion & chunking");
        System.out.println("  embed <text>  - Generate vector embedding for text");
        System.out.println("  index         - Index knowledge chunks into vector store");
        System.out.println("  search <text> - Perform Top-K exact similarity retrieval");
        System.out.println("  rag <text>    - Perform end-to-end RAG answer generation");
        System.out.println("  agent <text>  - Execute controlled knowledge agent");
        System.out.println("--------------------------------------------------");
        System.out.println("Application completed execution cleanly.");
    }
}
