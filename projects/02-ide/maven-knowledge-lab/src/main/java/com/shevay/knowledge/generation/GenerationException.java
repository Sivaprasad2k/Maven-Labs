package com.shevay.knowledge.generation;

/**
 * Runtime exception thrown when an LLM text generation operation fails,
 * times out, encounters HTTP errors, or receives malformed response payloads.
 */
public class GenerationException extends RuntimeException {

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
