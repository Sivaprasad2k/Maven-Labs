package com.shevay.knowledge.generation;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.Embedding;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingPurpose;
import com.shevay.knowledge.model.RagResponse;
import com.shevay.knowledge.model.RetrievedChunk;
import com.shevay.knowledge.model.SourceReference;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Primary orchestrator service for Phase 5 Retrieval-Augmented Generation (RAG).
 *
 * <p>Pipeline execution order:
 * User Query -> EmbeddingProvider -> SimilaritySearchService -> RetrievedChunk[]
 *   -> ContextAssembler -> PromptBuilder -> LlmGenerationProvider -> RagResponse</p>
 *
 * <p>Key constraints:
 * <ul>
 *   <li>Short-circuits immediately if retrieval yields zero qualifying chunks (no LLM invocation).</li>
 *   <li>Source attribution references are derived strictly from retrieved chunks, never synthesized by LLM.</li>
 * </ul>
 * </p>
 */
public class RagService {

    public static final String NO_CONTEXT_RESPONSE_PREFIX = "No relevant knowledge context found for query: ";

    private final SimilaritySearchService searchService;
    private final EmbeddingProvider embeddingProvider;
    private final ContextAssembler contextAssembler;
    private final PromptBuilder promptBuilder;
    private final LlmGenerationProvider generationProvider;

    public RagService(AppConfig config, EmbeddingProvider embeddingProvider, LlmGenerationProvider generationProvider) {
        this(new SimilaritySearchService(config), embeddingProvider, new ContextAssembler(), new PromptBuilder(), generationProvider);
    }

    public RagService(SimilaritySearchService searchService,
                      EmbeddingProvider embeddingProvider,
                      ContextAssembler contextAssembler,
                      PromptBuilder promptBuilder,
                      LlmGenerationProvider generationProvider) {
        this.searchService = Objects.requireNonNull(searchService, "searchService must not be null");
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider, "embeddingProvider must not be null");
        this.contextAssembler = Objects.requireNonNull(contextAssembler, "contextAssembler must not be null");
        this.promptBuilder = Objects.requireNonNull(promptBuilder, "promptBuilder must not be null");
        this.generationProvider = Objects.requireNonNull(generationProvider, "generationProvider must not be null");
    }

    /**
     * Executes the complete RAG workflow for a user query against the given vector store using default config thresholds.
     *
     * @param queryText   User query string
     * @param vectorStore Target vector store containing indexed records
     * @return Complete RagResponse object
     */
    public RagResponse query(String queryText, VectorStore vectorStore) {
        return query(queryText, vectorStore, searchService.getTopK(), searchService.getMinSimilarity());
    }

    /**
     * Executes the RAG workflow with explicit topK and minSimilarity overrides.
     *
     * @param queryText             User query string
     * @param vectorStore          Target vector store
     * @param topKOverride         Maximum chunks to retrieve
     * @param minSimilarityOverride Minimum similarity threshold score
     * @return Complete RagResponse object
     */
    public RagResponse query(String queryText, VectorStore vectorStore, int topKOverride, double minSimilarityOverride) {
        if (queryText == null || queryText.isBlank()) {
            throw new IllegalArgumentException("Query text must not be null or blank");
        }
        Objects.requireNonNull(vectorStore, "vectorStore must not be null");

        Embedding queryEmbedding = embeddingProvider.embed(queryText, EmbeddingPurpose.QUERY);
        List<RetrievedChunk> retrievedChunks = searchService.search(queryEmbedding, vectorStore, topKOverride, minSimilarityOverride);

        if (retrievedChunks.isEmpty()) {
            String noContextMsg = NO_CONTEXT_RESPONSE_PREFIX + queryText.strip();
            return new RagResponse(queryText, noContextMsg, List.of(), List.of());
        }

        String assembledContext = contextAssembler.assembleContext(retrievedChunks);
        String prompt = promptBuilder.buildPrompt(queryText, assembledContext);

        String generatedAnswer = generationProvider.generate(prompt);

        List<SourceReference> sources = createSourceReferences(retrievedChunks);

        return new RagResponse(queryText, generatedAnswer, retrievedChunks, sources);
    }

    private static List<SourceReference> createSourceReferences(List<RetrievedChunk> retrievedChunks) {
        List<SourceReference> sources = new ArrayList<>(retrievedChunks.size());
        for (RetrievedChunk retrieved : retrievedChunks) {
            String docId = retrieved.chunk().documentId();
            String sourcePath = retrieved.chunk().sourcePath();
            String snippet = retrieved.chunk().text();
            double relevance = retrieved.similarityScore();

            sources.add(new SourceReference(docId, sourcePath, snippet, relevance));
        }
        return List.copyOf(sources);
    }

    public SimilaritySearchService getSearchService() {
        return searchService;
    }

    public EmbeddingProvider getEmbeddingProvider() {
        return embeddingProvider;
    }

    public LlmGenerationProvider getGenerationProvider() {
        return generationProvider;
    }
}
