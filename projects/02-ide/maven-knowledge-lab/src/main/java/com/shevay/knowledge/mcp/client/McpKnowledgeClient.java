package com.shevay.knowledge.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

import java.io.File;
import java.time.Duration;
import java.util.Map;

/**
 * MCP Client demonstration connecting to McpKnowledgeServer over STDIO transport.
 * Demonstrates protocol initialization, tool discovery, schema inspection, tool execution, and clean shutdown.
 */
public class McpKnowledgeClient {

    public static void main(String[] args) {
        McpKnowledgeClient client = new McpKnowledgeClient();
        client.runDemo("java/collections.md");
    }

    public String runDemo(String documentPath) {
        StringBuilder log = new StringBuilder();
        log.append("==================================================\n");
        log.append("MCP Knowledge Client Demonstration\n");
        log.append("==================================================\n\n");

        String javaCmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = resolveServerClasspath();

        ServerParameters serverParams = ServerParameters.builder(javaCmd)
                .args("-cp", classPath, "com.shevay.knowledge.mcp.server.McpKnowledgeServer")
                .build();

        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        StdioClientTransport transport = new StdioClientTransport(serverParams, new JacksonMcpJsonMapper(objectMapper));
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        try {
            log.append("1. Connecting to MCP Server via STDIO transport...\n");
            client.initialize();
            log.append("   Connected & Initialized successfully.\n\n");

            log.append("2. Discovering available tools...\n");
            ListToolsResult toolsResult = client.listTools();
            for (McpSchema.Tool tool : toolsResult.tools()) {
                log.append("   - Tool Found   : ").append(tool.name()).append("\n");
                log.append("     Description  : ").append(tool.description()).append("\n");
            }
            log.append("\n");

            log.append("3. Invoking tool 'getKnowledgeDocument' with path: \"").append(documentPath).append("\"...\n");
            CallToolResult callResult = client.callTool(new McpSchema.CallToolRequest("getKnowledgeDocument", Map.of("path", documentPath)));

            log.append("4. Tool Execution Outcome:\n");
            log.append("   - Is Error: ").append(callResult.isError()).append("\n");
            if (callResult.content() != null && !callResult.content().isEmpty()) {
                for (McpSchema.Content content : callResult.content()) {
                    if (content instanceof McpSchema.TextContent textContent) {
                        log.append("   - Content:\n").append(textContent.text()).append("\n");
                    }
                }
            }
            log.append("\n5. Closing MCP Client connection & terminating child process...\n");
            client.close();
            log.append("   Connection closed cleanly.\n");

        } catch (Exception e) {
            log.append("MCP Client Error: ").append(e.getMessage()).append("\n");
            try {
                client.close();
            } catch (Exception ignored) {}
        }

        return log.toString();
    }

    public static String resolveServerClasspath() {
        String systemCp = System.getProperty("java.class.path");
        try {
            java.net.URL loc = com.shevay.knowledge.mcp.server.McpKnowledgeServer.class.getProtectionDomain().getCodeSource().getLocation();
            if (loc != null) {
                java.nio.file.Path classesPath = java.nio.file.Paths.get(loc.toURI());
                String sep = System.getProperty("path.separator", ";");
                java.nio.file.Path parent = classesPath.getParent();
                if (parent != null) {
                    java.nio.file.Path libDir = parent.resolve("lib");
                    if (java.nio.file.Files.exists(libDir)) {
                        return classesPath.toAbsolutePath().toString() + sep + libDir.toAbsolutePath().toString() + File.separator + "*";
                    }
                }
                return classesPath.toAbsolutePath().toString() + sep + systemCp;
            }
        } catch (Exception ignored) {}
        return systemCp;
    }
}
