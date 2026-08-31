package com.shevay.knowledge.vector;

/**
 * Domain exception thrown for vector storage IO failures, format corruption, or dimension mismatches.
 */
public class VectorStoreException extends RuntimeException {

    public VectorStoreException(String message) {
        super(message);
    }

    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
