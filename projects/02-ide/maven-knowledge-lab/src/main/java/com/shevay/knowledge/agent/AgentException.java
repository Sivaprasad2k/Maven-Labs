package com.shevay.knowledge.agent;

/**
 * Runtime exception thrown when an unrecoverable failure occurs in the KnowledgeAgent pipeline.
 */
public class AgentException extends RuntimeException {

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
