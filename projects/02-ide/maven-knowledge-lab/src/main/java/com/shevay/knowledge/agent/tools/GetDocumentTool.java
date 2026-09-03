package com.shevay.knowledge.agent.tools;

import com.shevay.knowledge.agent.AgentTool;
import com.shevay.knowledge.agent.ToolResult;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.model.Document;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Agent tool retrieving full content of a document from the local knowledge corpus.
 * Strictly enforces path traversal protection and restricts access to approved corpus documents.
 */
public class GetDocumentTool implements AgentTool {

    public static final String NAME = "getDocument";

    private final AppConfig config;
    private final DocumentLoader documentLoader;

    public GetDocumentTool(AppConfig config) {
        this(config, new DocumentLoader());
    }

    public GetDocumentTool(AppConfig config, DocumentLoader documentLoader) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.documentLoader = Objects.requireNonNull(documentLoader, "documentLoader must not be null");
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Retrieves the full content of a document from the local knowledge corpus. Arguments: {\"documentPath\": \"<relative path inside knowledge directory>\"}";
    }

    @Override
    public ToolResult execute(Map<String, Object> arguments) {
        if (arguments == null || (!arguments.containsKey("documentPath") && !arguments.containsKey("path"))) {
            return ToolResult.failure(NAME, "Missing required argument 'documentPath'");
        }

        Object pathObj = arguments.getOrDefault("documentPath", arguments.get("path"));
        if (pathObj == null || !(pathObj instanceof String rawPath) || rawPath.isBlank()) {
            return ToolResult.failure(NAME, "Argument 'documentPath' must be a non-blank string");
        }

        String pathStr = rawPath.trim().replace('\\', '/');

        // Security Validation: Reject path traversal, absolute paths, and leading separators
        if (pathStr.contains("..") || pathStr.startsWith("/") || pathStr.matches("^[a-zA-Z]:.*")) {
            return ToolResult.failure(NAME, "Path traversal or absolute path rejected: " + rawPath);
        }

        try {
            List<Document> documents = documentLoader.loadDocuments(config.getKnowledgePath());

            Optional<Document> matchedDoc = documents.stream()
                    .filter(d -> d.sourcePath().replace('\\', '/').equalsIgnoreCase(pathStr)
                              || d.sourcePath().replace('\\', '/').endsWith("/" + pathStr)
                              || d.sourcePath().replace('\\', '/').equalsIgnoreCase("knowledge/" + pathStr))
                    .findFirst();

            if (matchedDoc.isEmpty()) {
                return ToolResult.failure(NAME, "Document not found in knowledge corpus: " + rawPath);
            }

            Document doc = matchedDoc.get();
            String output = String.format("Document Title: %s\nSource Path: %s\nContent:\n%s",
                    doc.title(), doc.sourcePath(), doc.content());

            return ToolResult.success(NAME, output);
        } catch (Exception e) {
            return ToolResult.failure(NAME, "Error retrieving document: " + e.getMessage());
        }
    }
}
