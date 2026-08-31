package com.shevay.knowledge.vector;

import com.shevay.knowledge.model.VectorRecord;

import java.util.List;
import java.util.Optional;

/**
 * Provider-independent vector storage abstraction.
 * Responsible for persistence, retrieval, sizing, and clearing of VectorRecord objects.
 */
public interface VectorStore {

    /**
     * Persists or updates a single VectorRecord.
     *
     * @param record VectorRecord to save
     * @throws VectorStoreException if IO or validation error occurs
     */
    void save(VectorRecord record);

    /**
     * Persists or updates a batch of VectorRecords.
     *
     * @param records List of VectorRecords to save
     * @throws VectorStoreException if IO or validation error occurs
     */
    void saveAll(List<VectorRecord> records);

    /**
     * Finds a VectorRecord by its unique ID.
     *
     * @param id VectorRecord identifier
     * @return Optional containing the record if found, empty otherwise
     */
    Optional<VectorRecord> findById(String id);

    /**
     * Returns all current authoritative VectorRecords in the store.
     *
     * @return List of all stored VectorRecords
     */
    List<VectorRecord> findAll();

    /**
     * Returns the total count of distinct active vector records in the store.
     *
     * @return active record count
     */
    int size();

    /**
     * Clears all stored vector records and resets storage.
     */
    void clear();
}
