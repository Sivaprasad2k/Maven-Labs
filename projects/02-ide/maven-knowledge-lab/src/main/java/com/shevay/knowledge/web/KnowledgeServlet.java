package com.shevay.knowledge.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.document.DocumentLoader;
import com.shevay.knowledge.model.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servlet handling GET /api/knowledge/documents and GET /api/knowledge/document requests.
 */
@WebServlet(urlPatterns = {"/api/knowledge/documents", "/api/knowledge/document"})
public class KnowledgeServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AppConfig config;
    private DocumentLoader documentLoader;

    public KnowledgeServlet() {
        this(null, null);
    }

    public KnowledgeServlet(AppConfig config, DocumentLoader documentLoader) {
        this.config = config;
        this.documentLoader = documentLoader;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.config == null) {
            this.config = AppConfig.loadDefaults();
        }
        if (this.documentLoader == null) {
            this.documentLoader = new DocumentLoader();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getServletPath();
        if (pathInfo.endsWith("/documents")) {
            handleListDocuments(resp);
        } else if (pathInfo.endsWith("/document")) {
            handleGetDocument(req, resp);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    private void handleListDocuments(HttpServletResponse resp) throws IOException {
        try {
            List<Document> docs = documentLoader.loadDocuments(config.getKnowledgePath());
            List<Map<String, String>> summaryList = docs.stream()
                    .map(d -> Map.of(
                            "id", d.id(),
                            "title", d.title(),
                            "sourcePath", d.sourcePath().replace('\\', '/')
                    ))
                    .collect(Collectors.toList());

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), summaryList);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading documents: " + e.getMessage());
        }
    }

    private void handleGetDocument(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawPath = req.getParameter("path");
        if (rawPath == null || rawPath.isBlank()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter 'path' is required");
            return;
        }

        String pathStr = rawPath.trim().replace('\\', '/');
        if (pathStr.contains("..") || pathStr.startsWith("/") || pathStr.matches("^[a-zA-Z]:.*")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Path traversal or absolute path rejected");
            return;
        }

        try {
            List<Document> docs = documentLoader.loadDocuments(config.getKnowledgePath());
            Optional<Document> matchedDoc = docs.stream()
                    .filter(d -> d.sourcePath().replace('\\', '/').equalsIgnoreCase(pathStr)
                              || d.sourcePath().replace('\\', '/').endsWith("/" + pathStr)
                              || d.sourcePath().replace('\\', '/').equalsIgnoreCase("knowledge/" + pathStr))
                    .findFirst();

            if (matchedDoc.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Document not found: " + rawPath);
                return;
            }

            Document doc = matchedDoc.get();
            Map<String, Object> docMap = Map.of(
                    "id", doc.id(),
                    "title", doc.title(),
                    "sourcePath", doc.sourcePath().replace('\\', '/'),
                    "content", doc.content()
            );

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), docMap);
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading document: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), Map.of("error", message));
    }
}
