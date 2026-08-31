# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a Java 17 CLI-based Retrieval-Augmented Generation (RAG) learning system designed to demonstrate end-to-end document processing, vector search, context construction, and LLM-assisted question answering over local knowledge repositories.

## Current Implementation Stage
Current Status: Stage 2 - Document Ingestion and Deterministic Chunking

Stage 2 implements recursive document discovery, UTF-8 file reading, SHA-256 deterministic document identity and content hashing, character-based text chunking with configurable overlap, ingestion result reporting, CLI integration, and offline unit testing.

EXPLICIT STATEMENT: Vector embeddings (Stage 3), vector storage (Stage 4), cosine similarity retrieval (Stage 5), context construction and LLM answer generation (Stage 6-8) ARE NOT IMPLEMENTED YET IN STAGE 2.

## Ingestion Architecture
The Stage 2 pipeline operates as follows:
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
IngestionResult (Observability metrics: discovered, processed, chunks, skipped, failures)
```

## Core Abstractions & Rules

### Supported Document Types
- Supported extensions: `.md`, `.txt`
- Encoding: UTF-8 (explicitly enforced)
- Empty File Policy: Empty or whitespace-only files are skipped cleanly during ingestion.
- Unsupported Extensions: Non-text/binary files are ignored.

### Identity and Hashing Strategy
- **Document ID**: Calculated via `SHA-256(normalizedRelativePath)` relative to knowledge root. Absolute machine-specific directory paths are ignored to ensure cross-machine determinism.
- **Content Hash**: Calculated via `SHA-256(utf8Content)` over raw document text.
- **Chunk ID**: Calculated via `SHA-256(documentId + ":" + chunkIndex + ":" + chunkText)` canonical string format.

### Chunking Configuration
Configured in `AppConfig` properties (or system environment variables):
- `chunking.chunk-size`: 800 characters (default)
- `chunking.chunk-overlap`: 100 characters (default)

Validation rules enforced at configuration startup:
- `chunkSize > 0`
- `chunkOverlap >= 0`
- `chunkOverlap < chunkSize`

## Current Capabilities
Stage 2 provides:
- Recursive file loader (`DocumentLoader`) supporting nested directories.
- Deterministic text chunker (`TextChunker`) with whitespace boundary lookback.
- Ingestion service (`DocumentIngestionService`) producing `IngestionResult`.
- CLI sub-command (`ingest`) for executing document processing.
- Comprehensive unit test suite (29 tests) verifying loading, chunking, hashing, and configuration bounds.

## Maven Commands
Execute commands from the project root (`projects/02-ide/maven-knowledge-lab/`):

- Display Maven version:
  ```cmd
  .\mvnw.cmd --version
  ```

- Validate project structure:
  ```cmd
  .\mvnw.cmd clean validate
  ```

- Run unit tests:
  ```cmd
  .\mvnw.cmd test
  ```

- Compile and package executable JAR:
  ```cmd
  .\mvnw.cmd package
  ```

- Print dependency tree:
  ```cmd
  .\mvnw.cmd dependency:tree
  ```

- Execute document ingestion via JAR:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar ingest
  ```

- Execute standard application startup:
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
│   └── .gitkeep
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
    │   │       ├── model/
    │   │       │   ├── Document.java
    │   │       │   ├── DocumentChunk.java
    │   │       │   ├── VectorRecord.java
    │   │       │   ├── RetrievedChunk.java
    │   │       │   ├── SourceReference.java
    │   │       │   └── RagResponse.java
    │   │       └── util/
    │   │           └── HashUtil.java
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
                └── model/
                    └── DomainModelsTest.java
```
