package com.shevay.knowledge.vector;

import com.shevay.knowledge.config.AppConfig;
import com.shevay.knowledge.model.VectorRecord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * File-backed implementation of VectorStore using an explicit binary format with append-only persistence.
 *
 * <p>Physical File Layout (data/vectors.dat):
 * <pre>
 * [HEADER] (12 bytes)
 *   - Magic Identifier (4 bytes): 0x4D4B4C56 ("MKLV")
 *   - Format Version (4 bytes): 1
 *   - Vector Dimensions (4 bytes): e.g. 768
 * [RECORD 1]
 *   - Record Payload Length (4 bytes)
 *   - Vector ID (UTF string)
 *   - Chunk ID (UTF string)
 *   - Document ID (UTF string)
 *   - Source Path (UTF string)
 *   - Chunk Index (int 4 bytes)
 *   - Chunk Text (UTF string)
 *   - Token Count (int 4 bytes)
 *   - Vector Length (int 4 bytes)
 *   - Vector Float Values (float array, dimensions * 4 bytes)
 * [RECORD 2]
 * ...
 * </pre>
 * </p>
 */
public class FileVectorStore implements VectorStore {

    public static final int MAGIC_BYTES = 0x4D4B4C56; // "MKLV"
    public static final int FORMAT_VERSION = 1;
    public static final int HEADER_SIZE = 12;

    private final Path filePath;
    private final int expectedDimensions;
    private final Map<String, Long> index = new LinkedHashMap<>();

    public FileVectorStore(AppConfig config) {
        Objects.requireNonNull(config, "AppConfig must not be null");
        this.filePath = AppConfig.resolvePath(config.getVectorStorePath());
        this.expectedDimensions = config.getEmbeddingDimensions();
        initializeStorage();
    }

    public FileVectorStore(Path filePath, int expectedDimensions) {
        this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
        if (expectedDimensions <= 0) {
            throw new IllegalArgumentException("expectedDimensions must be positive: " + expectedDimensions);
        }
        this.expectedDimensions = expectedDimensions;
        initializeStorage();
    }

    private synchronized void initializeStorage() {
        try {
            File file = filePath.toFile();
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                Files.createDirectories(file.getParentFile().toPath());
            }

            if (!file.exists()) {
                writeHeaderNewFile(file);
                return;
            }

            // Existing file: validate header and scan index
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                if (raf.length() < HEADER_SIZE) {
                    throw new VectorStoreException("Truncated vector store file (size " + raf.length() + " < header size 12)");
                }

                int magic = raf.readInt();
                if (magic != MAGIC_BYTES) {
                    throw new VectorStoreException(String.format("Invalid vector store magic: 0x%08X (expected 0x%08X)", magic, MAGIC_BYTES));
                }

                int version = raf.readInt();
                if (version != FORMAT_VERSION) {
                    throw new VectorStoreException("Unsupported format version " + version + " (expected " + FORMAT_VERSION + ")");
                }

                int dimensions = raf.readInt();
                if (dimensions != expectedDimensions) {
                    throw new VectorStoreException("Stored vector dimensions (" + dimensions + ") do not match configured dimensions (" + expectedDimensions + ")");
                }

                // Scan records to reconstruct index
                index.clear();
                long fileLength = raf.length();
                while (raf.getFilePointer() < fileLength) {
                    long offset = raf.getFilePointer();
                    if (fileLength - offset < 4) {
                        throw new VectorStoreException("Truncated record header at offset " + offset);
                    }
                    int payloadLen = raf.readInt();
                    if (payloadLen <= 0 || (raf.getFilePointer() + payloadLen) > fileLength) {
                        throw new VectorStoreException("Invalid or corrupt record length " + payloadLen + " at offset " + offset);
                    }

                    byte[] payload = new byte[payloadLen];
                    raf.readFully(payload);
                    VectorRecord record = deserializeRecordPayload(payload, expectedDimensions, offset);
                    index.put(record.id(), offset);
                }
            }
        } catch (IOException e) {
            throw new VectorStoreException("Failed to initialize FileVectorStore at " + filePath + ": " + e.getMessage(), e);
        }
    }

    private void writeHeaderNewFile(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(0);
            raf.writeInt(MAGIC_BYTES);
            raf.writeInt(FORMAT_VERSION);
            raf.writeInt(expectedDimensions);
            raf.getChannel().force(true);
        }
        index.clear();
    }

    @Override
    public synchronized void save(VectorRecord record) {
        Objects.requireNonNull(record, "VectorRecord must not be null");
        validateRecord(record);

        try {
            byte[] payload = serializeRecordPayload(record);
            File file = filePath.toFile();
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                long offset = raf.length();
                raf.seek(offset);
                raf.writeInt(payload.length);
                raf.write(payload);
                raf.getChannel().force(true);

                index.put(record.id(), offset);
            }
        } catch (IOException e) {
            throw new VectorStoreException("IO failure while saving VectorRecord id '" + record.id() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void saveAll(List<VectorRecord> records) {
        Objects.requireNonNull(records, "Records list must not be null");
        if (records.isEmpty()) {
            return;
        }

        for (VectorRecord record : records) {
            save(record);
        }
    }

    @Override
    public synchronized Optional<VectorRecord> findById(String id) {
        Objects.requireNonNull(id, "VectorRecord id must not be null");
        Long offset = index.get(id);
        if (offset == null) {
            return Optional.empty();
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            raf.seek(offset);
            int payloadLen = raf.readInt();
            byte[] payload = new byte[payloadLen];
            raf.readFully(payload);
            return Optional.of(deserializeRecordPayload(payload, expectedDimensions, offset));
        } catch (IOException e) {
            throw new VectorStoreException("IO failure while reading VectorRecord id '" + id + "' at offset " + offset + ": " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized List<VectorRecord> findAll() {
        List<VectorRecord> result = new ArrayList<>(index.size());
        if (index.isEmpty()) {
            return result;
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            for (Long offset : index.values()) {
                raf.seek(offset);
                int payloadLen = raf.readInt();
                byte[] payload = new byte[payloadLen];
                raf.readFully(payload);
                result.add(deserializeRecordPayload(payload, expectedDimensions, offset));
            }
            return result;
        } catch (IOException e) {
            throw new VectorStoreException("IO failure while scanning all VectorRecords from " + filePath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized int size() {
        return index.size();
    }

    @Override
    public synchronized void clear() {
        try {
            writeHeaderNewFile(filePath.toFile());
        } catch (IOException e) {
            throw new VectorStoreException("Failed to clear vector store at " + filePath + ": " + e.getMessage(), e);
        }
    }

    public Path getFilePath() {
        return filePath;
    }

    private void validateRecord(VectorRecord record) {
        if (record.dimensions() != expectedDimensions) {
            throw new VectorStoreException(String.format(
                    "Record dimension mismatch: record has %d dimensions but store expects %d",
                    record.dimensions(), expectedDimensions));
        }
        float[] v = record.vector();
        for (int i = 0; i < v.length; i++) {
            if (Float.isNaN(v[i]) || Float.isInfinite(v[i])) {
                throw new VectorStoreException(String.format("Record vector contains non-finite float at index %d: %f", i, v[i]));
            }
        }
    }

    private static byte[] serializeRecordPayload(VectorRecord record) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeUTF(record.id());
        dos.writeUTF(record.chunkId());
        dos.writeUTF(record.documentId());
        dos.writeUTF(record.sourcePath());
        dos.writeInt(record.chunkIndex());
        dos.writeUTF(record.text());
        dos.writeInt(record.tokenCount());
        dos.writeInt(record.dimensions());

        float[] vec = record.vector();
        for (float val : vec) {
            dos.writeFloat(val);
        }

        dos.flush();
        return baos.toByteArray();
    }

    private static VectorRecord deserializeRecordPayload(byte[] payload, int expectedDimensions, long offset) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(payload);
        DataInputStream dis = new DataInputStream(bais);

        try {
            String id = dis.readUTF();
            String chunkId = dis.readUTF();
            String documentId = dis.readUTF();
            String sourcePath = dis.readUTF();
            int chunkIndex = dis.readInt();
            String text = dis.readUTF();
            int tokenCount = dis.readInt();
            int dimensions = dis.readInt();

            if (dimensions != expectedDimensions) {
                throw new VectorStoreException(String.format(
                        "Corrupt record at offset %d: record dimensions %d do not match expected %d",
                        offset, dimensions, expectedDimensions));
            }

            float[] vec = new float[dimensions];
            for (int i = 0; i < dimensions; i++) {
                vec[i] = dis.readFloat();
                if (Float.isNaN(vec[i]) || Float.isInfinite(vec[i])) {
                    throw new VectorStoreException(String.format(
                            "Corrupt record at offset %d: non-finite float at vector index %d: %f",
                            offset, i, vec[i]));
                }
            }

            return new VectorRecord(id, chunkId, documentId, sourcePath, chunkIndex, text, tokenCount, vec, dimensions);
        } catch (VectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw new VectorStoreException("Corrupt record format at offset " + offset + ": " + e.getMessage(), e);
        }
    }
}
