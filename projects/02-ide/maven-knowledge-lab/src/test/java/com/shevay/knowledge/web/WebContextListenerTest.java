package com.shevay.knowledge.web;

import com.shevay.knowledge.embedding.GeminiEmbeddingProvider;
import com.shevay.knowledge.generation.RagService;
import com.shevay.knowledge.vector.VectorStore;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebContextListenerTest {

    @Test
    @DisplayName("Should initialize RagService and VectorStore in ServletContext using system property key")
    void testContextInitializedWithSystemPropertyKey() {
        String origKey = System.getProperty("gemini.api.key");
        try {
            System.setProperty("gemini.api.key", "test-valid-api-key-999");

            Map<String, Object> attributes = new HashMap<>();
            ServletContext mockContext = createMockServletContext(attributes);
            ServletContextEvent event = new ServletContextEvent(mockContext);

            WebContextListener listener = new WebContextListener();
            listener.contextInitialized(event);

            Object ragServiceObj = attributes.get(WebContextListener.RAG_SERVICE_ATTRIBUTE);
            Object vectorStoreObj = attributes.get(WebContextListener.VECTOR_STORE_ATTRIBUTE);

            assertNotNull(ragServiceObj, "RagService must be bound to ServletContext");
            assertNotNull(vectorStoreObj, "VectorStore must be bound to ServletContext");
            assertTrue(ragServiceObj instanceof RagService);
            assertTrue(vectorStoreObj instanceof VectorStore);

            RagService ragService = (RagService) ragServiceObj;
            assertTrue(ragService.getEmbeddingProvider() instanceof GeminiEmbeddingProvider,
                    "Provider must be GeminiEmbeddingProvider when valid gemini.api.key property is present");
        } finally {
            if (origKey != null) {
                System.setProperty("gemini.api.key", origKey);
            } else {
                System.clearProperty("gemini.api.key");
            }
        }
    }

    @Test
    @DisplayName("Should fall back to DummyEmbeddingProvider when gemini.api.key is empty")
    void testContextInitializedWithMissingKeyFallback() {
        String origKey = System.getProperty("gemini.api.key");
        try {
            System.setProperty("gemini.api.key", "");

            Map<String, Object> attributes = new HashMap<>();
            ServletContext mockContext = createMockServletContext(attributes);
            ServletContextEvent event = new ServletContextEvent(mockContext);

            WebContextListener listener = new WebContextListener();
            listener.contextInitialized(event);

            Object ragServiceObj = attributes.get(WebContextListener.RAG_SERVICE_ATTRIBUTE);
            assertNotNull(ragServiceObj);
            assertTrue(ragServiceObj instanceof RagService);

            RagService ragService = (RagService) ragServiceObj;
            assertTrue(ragService.getEmbeddingProvider() instanceof com.shevay.knowledge.embedding.DummyEmbeddingProvider);
        } finally {
            if (origKey != null) {
                System.setProperty("gemini.api.key", origKey);
            } else {
                System.clearProperty("gemini.api.key");
            }
        }
    }

    @Test
    @DisplayName("Should remove attributes from ServletContext on contextDestroyed")
    void testContextDestroyedRemovesAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(WebContextListener.RAG_SERVICE_ATTRIBUTE, "dummyService");
        attributes.put(WebContextListener.VECTOR_STORE_ATTRIBUTE, "dummyStore");

        ServletContext mockContext = createMockServletContext(attributes);
        ServletContextEvent event = new ServletContextEvent(mockContext);

        WebContextListener listener = new WebContextListener();
        listener.contextDestroyed(event);

        assertFalse(attributes.containsKey(WebContextListener.RAG_SERVICE_ATTRIBUTE));
        assertFalse(attributes.containsKey(WebContextListener.VECTOR_STORE_ATTRIBUTE));
    }

    private static ServletContext createMockServletContext(Map<String, Object> attributes) {
        return (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class<?>[]{ServletContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRealPath" -> null;
                    case "setAttribute" -> {
                        attributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getAttribute" -> attributes.get((String) args[0]);
                    case "removeAttribute" -> {
                        attributes.remove((String) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }
}
