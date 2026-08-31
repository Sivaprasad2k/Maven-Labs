# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a Java 17 CLI-based Retrieval-Augmented Generation (RAG) learning system designed to demonstrate end-to-end document processing, vector embeddings, local vector storage, exact similarity retrieval, context construction, and LLM-assisted question answering over local knowledge repositories.

## Current Implementation Stage
Current Status: Phase 4 - Persistent Vector Store and Similarity Retrieval

Phase 4 implements a persistent, provider-independent vector storage layer (`FileVectorStore`), explicit binary format (`data/vectors.dat`), append-only persistence with startup index reconstruction (`Map<String, Long>`), exact linear cosine similarity search (`CosineSimilarity`), threshold filtering, Top-K ranking with deterministic tie-breaking (`SimilaritySearchService`), CLI indexing and search sub-commands (`index`, `search`), and a comprehensive offline test suite.

EXPLICIT STATEMENT: Natural language LLM answer generation (Phase 5-8), prompt construction, and conversational RAG orchestration ARE NOT IMPLEMENTED YET IN PHASE 4. `RetrievedChunk` results represent the final boundary of Phase 4.

## System Architecture
The Phase 1 through Phase 4 pipeline operates as follows:
```text
knowledge/ Directory
       │
       ▼
DocumentLoader (Recursive discovery, UTF-8 reading, .md/.txt filtering)
       │
       ▼
Document (SHA-256 relative path ID, SHA-256 content hash, title, metadata)
       │
       ▼
TextChunker (Deterministic character windowing, configurable size & overlap)
       │
       ▼
DocumentChunk (SHA-256 canonical chunk ID, index, text, token count)
       │
       ▼
EmbeddingPurpose (DOCUMENT | QUERY)
       │
       ▼
EmbeddingProvider (Interface: GeminiEmbeddingProvider | DummyEmbeddingProvider)
       │
       ▼
Embedding (Immutable L2-normalized 768-dim float vector)
       │
       ▼
VectorRecord (id, chunkId, documentId, sourcePath, chunkIndex, text, tokenCount, vector, dimensions)
       │
       ▼
VectorStore (FileVectorStore: binary data/vectors.dat + runtime Map<String, Long> offset index)
       │
       ▼
CosineSimilarity (Pure double-precision dot product over L2-normalized vectors)
       │
       ▼
SimilaritySearchService (Linear search, min-similarity cutoff, Top-K ranking, deterministic tie-breaking)
       │
       ▼
RetrievedChunk (DocumentChunk + similarityScore)
```

## Binary Storage Format (`data/vectors.dat`)

The `FileVectorStore` uses an explicit, deterministic binary layout built with standard Java `DataOutputStream` / `DataInputStream` primitive encodings:

### Header Layout (12 bytes)
| Field | Type | Size | Description |
| :--- | :--- | :--- | :--- |
| Magic Identifier | `int` | 4 bytes | `0x4D4B4C56` (`"MKLV"` in ASCII) |
| Format Version | `int` | 4 bytes | `1` |
| Vector Dimensions | `int` | 4 bytes | `768` (Configured vector length) |

### Record Framing Layout (Append-Only)
| Field | Type | Size | Description |
| :--- | :--- | :--- | :--- |
| Record Payload Length | `int` | 4 bytes | Byte count of payload following length |
| Vector ID | `UTF-8 String` | Variable | Unique vector record identifier (`"vec-" + chunkId`) |
| Chunk ID | `UTF-8 String` | Variable | SHA-256 chunk identifier |
| Document ID | `UTF-8 String` | Variable | SHA-256 document identifier |
| Source Path | `UTF-8 String` | Variable | Relative file path |
| Chunk Index | `int` | 4 bytes | Zero-based chunk index in document |
| Chunk Text | `UTF-8 String` | Variable | Raw chunk text content |
| Token Count | `int` | 4 bytes | Estimated token count |
| Vector Length | `int` | 4 bytes | Vector array length (`768`) |
| Vector Float Values | `float[]` | `dimensions * 4` bytes | Float array of normalized embedding values |

## Core Components & Mechanics

### VectorStore Abstraction
`VectorStore` provides a provider-independent persistence contract:
- `save(VectorRecord)`: Appends record to binary storage and updates runtime offset index.
- `saveAll(List<VectorRecord>)`: Batch save of vector records.
- `findById(String id)`: Looks up byte offset in runtime index and seeks to read exact record.
- `findAll()`: Returns all active authoritative `VectorRecord` objects in offset order.
- `size()`: Returns distinct active vector record count.
- `clear()`: Overwrites binary file with header and resets runtime index.

### Startup Index Reconstruction
At application startup, `FileVectorStore` validates the 12-byte header (`MKLV` magic, version `1`, dimensions `768`), then performs a single sequential scan from byte offset 12 to EOF. It constructs an in-memory `Map<String, Long>` (`vectorId -> fileOffset`).

### Append-Only & Upsert Semantics
When updating an existing vector ID, the store appends a new record payload to the end of `vectors.dat` and updates the runtime index entry to point to the newest byte offset. Historical records remain physically untouched, guaranteeing append-only write performance without full-file rewrites.

### Cosine Similarity & Numeric Precision
`CosineSimilarity` computes:
$$\text{cos}(A, B) = \frac{A \cdot B}{\|A\|_2 \|B\|_2}$$
Accumulation uses `double` precision to eliminate float rounding errors. Null inputs, dimension mismatches, non-finite float values (`NaN`, `Infinity`), and zero-magnitude vectors ($< 10^{-12}$) are strictly rejected with explicit exceptions.

### Deterministic Top-K Ranking
`SimilaritySearchService` evaluates stored vectors against the query vector:
1. Calculates similarity score for all candidates.
2. Filters out candidates below `minSimilarity` threshold (default `0.70`).
3. Sorts candidates by `similarityScore` descending.
4. Breaks ties deterministically by `chunkId` ascending.
5. Truncates results to `topK` (default `3`).

## Engineering Decisions

1. **Why append-only storage?** Append operations require $O(1)$ disk writes without full-file rewrites or complex free-list page management.
2. **Why binary format rather than Java object serialization?** Native Java serialization (`ObjectOutputStream`) is slow, non-portable, unsafe, and brittle across JVM versions. The explicit binary format is self-describing, versioned, and deterministic.
3. **Why runtime HashMap index?** For small to medium local knowledge corpora ($< 100,000$ vectors), an in-memory byte-offset index provides fast $O(1)$ lookup while keeping disk storage as the single source of truth.
4. **Why rebuild index at startup?** Rebuilding the index from the append log avoids dual-write consistency issues between a data file and a separate index file.
5. **Why no persistent index on disk?** A separate persistent index adds complexity without benefit for local laboratory scale.
6. **Why exact linear search?** $O(N \times D)$ exact linear search guarantees 100% recall accuracy. Understanding exact retrieval mechanics is required before studying approximate algorithms.
7. **Why no HNSW/FAISS?** Graph-based (HNSW) or inverted index (IVF) approximate nearest neighbor algorithms introduce complex hyperparameter tuning and approximate recall trade-offs unnecessary for local corpora.
8. **Why no PostgreSQL/pgVector?** Keeping Phase 4 in pure Java 17 eliminates external server, Docker, database setup, and credential overhead.
9. **Why VectorStore is independent of EmbeddingProvider?** Complete decoupling guarantees vector storage can consume embeddings from Gemini, Voyage, OpenAI, Cohere, or local models without modifying storage logic.
10. **Why physical deletion is not implemented?** Physical deletion requires file compaction. Append-only upsert semantics meet Phase 4 requirements without compaction overhead.
11. **Why upsert creates a newer physical record?** Appending new record versions preserves append-only write simplicity while updating the runtime offset index to point to the newest record.
12. **Why dimensions are strictly validated?** Mixing vectors of different dimensions (e.g. 384 vs 768) produces invalid mathematical dot products. Strict validation prevents silent corruption.

## Configuration Properties

Configured in `AppConfig` properties or environment variables:
- `knowledge.path`: `knowledge` (default)
- `data.path`: `data` (default)
- `vector.store.path`: `data/vectors.dat` (default)
- `retrieval.top-k`: `3` (default)
- `retrieval.min-similarity`: `0.7` (default)
- `embedding.provider`: `gemini` (default)
- `embedding.model`: `gemini-embedding-001` (default)
- `embedding.dimensions`: `768` (default)
- `embedding.timeout-seconds`: `30` (default)

## Maven & CLI Commands

Execute commands from the project directory (`projects/02-ide/maven-knowledge-lab/`):

- Validate project structure:
  ```cmd
  .\mvnw.cmd clean validate
  ```

- Run full offline test suite (79 total tests):
  ```cmd
  .\mvnw.cmd test
  ```

- Run opt-in live Gemini API integration test (requires `GEMINI_API_KEY`):
  ```cmd
  .\mvnw.cmd test -Dgemini.integration=true
  ```

- Compile and package executable JAR:
  ```cmd
  .\mvnw.cmd package
  ```

- Execute vector indexing CLI:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar index
  ```

- Execute exact similarity search CLI:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar search "What is the Maven build lifecycle?"
  ```

- Print standard application information:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar
  ```

## Project Structure
```
projects/02-ide/maven-knowledge-lab/
├── .gitignore
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── knowledge/
│   ├── java/
│   │   ├── collections.md
│   │   └── concurrency.md
│   └── maven/
│       ├── dependencies.md
│       └── lifecycle.md
├── data/
│   ├── .gitkeep
│   └── vectors.dat
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/shevay/knowledge/
    │   │       ├── Application.java
    │   │       ├── config/
    │   │       │   └── AppConfig.java
    │   │       ├── document/
    │   │       │   ├── DocumentIngestionService.java
    │   │       │   ├── DocumentLoader.java
    │   │       │   ├── IngestionResult.java
    │   │       │   └── TextChunker.java
    │   │       ├── embedding/
    │   │       │   ├── DummyEmbeddingProvider.java
    │   │       │   ├── Embedding.java
    │   │       │   ├── EmbeddingException.java
    │   │       │   ├── EmbeddingProvider.java
    │   │       │   ├── EmbeddingPurpose.java
    │   │       │   ├── GeminiEmbeddingProvider.java
    │   │       │   └── VectorNormalizer.java
    │   │       ├── model/
    │   │       │   ├── Document.java
    │   │       │   ├── DocumentChunk.java
    │   │       │   ├── VectorRecord.java
    │   │       │   ├── RetrievedChunk.java
    │   │       │   ├── SourceReference.java
    │   │       │   └── RagResponse.java
    │   │       ├── retrieval/
    │   │       │   ├── CosineSimilarity.java
    │   │       │   └── SimilaritySearchService.java
    │   │       ├── util/
    │   │       │   └── HashUtil.java
    │   │       └── vector/
    │   │           ├── FileVectorStore.java
    │   │           ├── VectorStore.java
    │   │           └── VectorStoreException.java
    │   └── resources/
    └── test/
        └── java/
            └── com/shevay/knowledge/
                ├── ApplicationTest.java
                ├── config/
                │   └── AppConfigTest.java
                ├── document/
                │   ├── ChunkIdentityTest.java
                │   ├── ContentHashTest.java
                │   ├── DocumentIdentityTest.java
                │   ├── DocumentIngestionServiceTest.java
                │   ├── DocumentLoaderTest.java
                │   └── TextChunkerTest.java
                ├── embedding/
                │   ├── DummyEmbeddingProviderTest.java
                │   ├── EmbeddingNormalizationTest.java
                │   ├── EmbeddingTest.java
                │   ├── GeminiEmbeddingProviderTest.java
                │   └── GeminiIntegrationTest.java
                ├── model/
                │   └── DomainModelsTest.java
                ├── retrieval/
                │   ├── CosineSimilarityTest.java
                │   └── SimilaritySearchServiceTest.java
                └── vector/
                    ├── FileVectorStoreTest.java
                    └── VectorStoreCorruptionTest.java
```

## Future Migration Path to pgVector
When transitioning from the local binary `FileVectorStore` to a production PostgreSQL + `pgVector` database in future stages:
1. `VectorStore` interface remains unchanged.
2. `PgVectorStore` implementation will map `VectorRecord` to a PostgreSQL table `vector_records (id VARCHAR PRIMARY KEY, chunk_id VARCHAR, embedding vector(768), metadata JSONB)`.
3. Cosine similarity operations will delegate to native pgVector SQL index operators (`<=>` cosine distance operator).
4. `SimilaritySearchService` and `EmbeddingProvider` remain unaffected due to strict interface decoupling.
