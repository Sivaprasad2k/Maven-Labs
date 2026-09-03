package com.shevay.knowledge.web;

/**
 * Immutable DTO record for receiving RAG query JSON payloads.
 */
public record RagQueryRequest(String query) {
}
