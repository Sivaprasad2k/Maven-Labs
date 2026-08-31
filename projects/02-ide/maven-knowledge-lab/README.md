# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a Java 17 CLI-based Retrieval-Augmented Generation (RAG) learning system designed to demonstrate end-to-end document processing, vector embeddings, local vector storage, context construction, and LLM-assisted question answering over local knowledge repositories.

## Current Implementation Stage
Current Status: Phase 3 - Embedding Provider

Phase 3 implements a provider-neutral embedding abstraction, L2 vector normalization, a production REST API client for Google Gemini `gemini-embedding-001` (768 dimensions), a deterministic offline test provider (`DummyEmbeddingProvider`), CLI embedding integration, and a comprehensive test suite with opt-in live integration testing.

EXPLICIT STATEMENT: Vector database storage (Phase 4), similarity search and cosine ranking (Phase 5), context construction and LLM answer generation (Phase 6-8) ARE NOT IMPLEMENTED YET IN PHASE 3.

## System Architecture
The Phase 1 through Phase 3 pipeline operates as follows:
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
EmbeddingProvider (Interface)
       │
       ├─────────────────────────────────┐
       ▼                                 ▼
DummyEmbeddingProvider             GeminiEmbeddingProvider
(Deterministic 768-dim offline)    (Java 17 HttpClient REST to gemini-embedding-001)
       │                                 │
       └────────────────┬────────────────┘
                        ▼
               VectorNormalizer (L2)
                        │
                        ▼
                Embedding (Immutable L2-normalized 768-dim float vector)
```

## Core Abstractions & Rules

### Embedding Model & Provider Contract
- **Provider**: Google Gemini API (`gemini`)
- **Model Identifier**: `gemini-embedding-001`
- **Output Dimensions**: 768
- **Purpose Semantics**:
  - Domain `DOCUMENT` maps to Gemini `RETRIEVAL_DOCUMENT` (for document/chunk indexing).
  - Domain `QUERY` maps to Gemini `RETRIEVAL_QUERY` (for user search queries).

### Vector Normalization
- All generated vectors are L2-normalized: $v_{norm} = v / \|v\|_2$ where $\|v\|_2 = \sqrt{\sum v_i^2}$.
- Zero or near-zero magnitude vectors are rejected explicitly without producing `NaN` or `Infinity`.

### API Key Security
- Authentication relies exclusively on the `GEMINI_API_KEY` environment variable.
- The API key is NEVER stored in `application.properties`, NEVER printed to standard output or logs, and NEVER committed to Git.

### Configuration Properties
Configured in `AppConfig` properties (or system environment variables):
- `embedding.provider`: `gemini` (default)
- `embedding.model`: `gemini-embedding-001` (default)
- `embedding.dimensions`: `768` (default)
- `embedding.timeout-seconds`: `30` (default)

Validation rules enforced at configuration startup:
- `embeddingProvider == "gemini"`
- `embeddingModel == "gemini-embedding-001"`
- `embeddingDimensions == 768`
- `embeddingTimeoutSeconds > 0`

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

- Run unit test suite (100% offline, zero network calls):
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

- Print dependency tree:
  ```cmd
  .\mvnw.cmd dependency:tree
  ```

- Execute document ingestion via CLI:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar ingest
  ```

- Execute embedding generation via CLI:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar embed "What is the Maven lifecycle?"
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
                ├── embedding/
                │   ├── DummyEmbeddingProviderTest.java
                │   ├── EmbeddingNormalizationTest.java
                │   ├── EmbeddingTest.java
                │   ├── GeminiEmbeddingProviderTest.java
                │   └── GeminiIntegrationTest.java
                └── model/
                    └── DomainModelsTest.java
```
