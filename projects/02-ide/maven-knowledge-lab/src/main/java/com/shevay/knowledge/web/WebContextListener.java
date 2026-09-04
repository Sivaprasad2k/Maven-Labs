package com.shevay.knowledge.web;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.embedding.DummyEmbeddingProvider;
import com.shevay.knowledge.embedding.EmbeddingProvider;
import com.shevay.knowledge.embedding.GeminiEmbeddingProvider;
import com.shevay.knowledge.generation.ContextAssembler;
import com.shevay.knowledge.generation.DummyLlmGenerationProvider;
import com.shevay.knowledge.generation.GeminiGenerationProvider;
import com.shevay.knowledge.generation.LlmGenerationProvider;
import com.shevay.knowledge.generation.PromptBuilder;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.retrieval.SimilaritySearchService;
import com.shevay.knowledge.vector.FileVectorStore;
import com.shevay.knowledge.vector.VectorStore;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Web application lifecycle listener that initializes shared application singletons
 * once during container startup and binds them to the ServletContext.
 */
@WebListener
public class WebContextListener implements ServletContextListener {

    public static final String RAG_SERVICE_ATTRIBUTE = "ragService";
    public static final String VECTOR_STORE_ATTRIBUTE = "vectorStore";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String realPath = sce.getServletContext().getRealPath("/");
        if (realPath != null) {
            AppConfig.setServletContextRealPath(realPath);
        }
        AppConfig config = AppConfig.loadDefaults();

        EmbeddingProvider embeddingProvider = createEmbeddingProvider(config);
        VectorStore vectorStore = new FileVectorStore(config);
        SimilaritySearchService searchService = new SimilaritySearchService(config);
        LlmGenerationProvider generationProvider = createLlmGenerationProvider(config);

        RagService ragService = new RagService(
                searchService,
                embeddingProvider,
                new ContextAssembler(),
                new PromptBuilder(),
                generationProvider
        );

        sce.getServletContext().setAttribute(RAG_SERVICE_ATTRIBUTE, ragService);
        sce.getServletContext().setAttribute(VECTOR_STORE_ATTRIBUTE, vectorStore);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(RAG_SERVICE_ATTRIBUTE);
        sce.getServletContext().removeAttribute(VECTOR_STORE_ATTRIBUTE);
    }

    private static EmbeddingProvider createEmbeddingProvider(AppConfig config) {
        String providerName = config.getEmbeddingProvider();
        if ("dummy".equalsIgnoreCase(providerName) || "dummy".equalsIgnoreCase(System.getProperty("embedding.provider", ""))) {
            System.out.println("Gemini embedding provider unavailable; using dummy provider");
            return new DummyEmbeddingProvider(config.getEmbeddingDimensions());
        }
        String apiKey = config.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Gemini embedding provider unavailable; using dummy provider");
            return new DummyEmbeddingProvider(config.getEmbeddingDimensions());
        }
        System.out.println("Gemini embedding provider initialized");
        return new GeminiEmbeddingProvider(config);
    }

    private static LlmGenerationProvider createLlmGenerationProvider(AppConfig config) {
        String providerName = config.getGenerationProvider();
        if ("dummy".equalsIgnoreCase(providerName) || "dummy".equalsIgnoreCase(System.getProperty("generation.provider", ""))) {
            return new DummyLlmGenerationProvider();
        }
        String apiKey = config.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return new DummyLlmGenerationProvider();
        }
        return new GeminiGenerationProvider(config);
    }
}
