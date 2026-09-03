package com.shevay.knowledge.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.model.Document;

import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MCP Server entry point for Maven Knowledge Lab exposing the getKnowledgeDocument tool.
 * Operates over STDIO transport strictly reserving STDOUT for MCP protocol framing.
 */
public class McpKnowledgeServer {

    public static final String TOOL_NAME = "getKnowledgeDocument";
    private static final String TOOL_SCHEMA_JSON = """
            {
              "type": "object",
              "properties": {
                "path": {
                  "type": "string",
                  "description": "Relative document path inside knowledge directory (e.g. java/collections.md)"
                }
              },
              "required": ["path"]
            }
            """;

    private final AppConfig config;
    private final DocumentLoader documentLoader;
    private final ObjectMapper objectMapper;

    public McpKnowledgeServer() {
        this(AppConfig.loadDefaults(), new DocumentLoader());
    }

    public McpKnowledgeServer(AppConfig config, DocumentLoader documentLoader) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.documentLoader = Objects.requireNonNull(documentLoader, "documentLoader must not be null");
        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        System.err.println("[MCP Server] Starting Maven Knowledge Lab MCP Server on STDIO...");
        McpKnowledgeServer server = new McpKnowledgeServer();
        server.startAndBlock();
    }

    public void startAndBlock() {
        JacksonMcpJsonMapper mapper = new JacksonMcpJsonMapper(objectMapper);
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(mapper);

        Tool tool = Tool.builder(TOOL_NAME, mapper, TOOL_SCHEMA_JSON)
                .description("Retrieve a document from the controlled knowledge corpus by path.")
                .build();

        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("maven-knowledge-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .toolCall(tool, (exchange, request) -> handleGetKnowledgeDocument(request.arguments()))
                .build();

        System.err.println("[MCP Server] Server initialized on STDIO transport.");
    }

    public CallToolResult handleGetKnowledgeDocument(Map<String, Object> arguments) {
        if (arguments == null || !arguments.containsKey("path")) {
            return buildErrorResult("Missing required argument 'path'");
        }

        Object pathObj = arguments.get("path");
        if (pathObj == null || !(pathObj instanceof String rawPath) || rawPath.isBlank()) {
            return buildErrorResult("Argument 'path' must be a non-blank string");
        }

        String pathStr = rawPath.trim().replace('\\', '/');

        // Security Check: Reject path traversal, leading slashes, and Windows drive letters
        if (pathStr.contains("..") || pathStr.startsWith("/") || pathStr.matches("^[a-zA-Z]:.*")) {
            return buildErrorResult("Security Error: Path traversal or absolute path rejected: " + rawPath);
        }

        try {
            List<Document> documents = documentLoader.loadDocuments(config.getKnowledgePath());

            Optional<Document> matchedDoc = documents.stream()
                    .filter(d -> d.sourcePath().replace('\\', '/').equalsIgnoreCase(pathStr)
                              || d.sourcePath().replace('\\', '/').endsWith("/" + pathStr)
                              || d.sourcePath().replace('\\', '/').equalsIgnoreCase("knowledge/" + pathStr))
                    .findFirst();

            if (matchedDoc.isEmpty()) {
                return buildErrorResult("Document not found in knowledge corpus: " + rawPath);
            }

            Document doc = matchedDoc.get();
            Map<String, Object> structuredResult = Map.of(
                    "path", doc.sourcePath(),
                    "title", doc.title(),
                    "content", doc.content()
            );

            String jsonOutput = objectMapper.writeValueAsString(structuredResult);
            return CallToolResult.builder()
                    .content(List.of(new TextContent(jsonOutput)))
                    .isError(false)
                    .build();

        } catch (Exception e) {
            return buildErrorResult("Server Error reading document: " + e.getMessage());
        }
    }

    private static CallToolResult buildErrorResult(String message) {
        return CallToolResult.builder()
                .content(List.of(new TextContent("{\"error\":\"" + message.replace("\"", "\\\"") + "\"}")))
                .isError(true)
                .build();
    }
}
