# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a Java 17 Retrieval-Augmented Generation (RAG) learning system designed to demonstrate end-to-end document processing, vector embeddings, local vector storage, exact similarity retrieval, context construction, LLM-assisted answer generation, and Jakarta Servlet web deployment over local knowledge repositories.

## Current Implementation Stage
Current Status: Phase 6 - Servlet Web Interface

Phase 6 introduces a thin Jakarta Servlet 6.0 web layer running inside Apache Tomcat. It exposes the core RAG application pipeline over HTTP while maintaining complete separation between the web adapter and application core. The application packages as a WAR artifact (`target/maven-knowledge-lab-1.0-SNAPSHOT.war`) and retains full backwards-compatible CLI operation.

## System Architecture
The complete system operates as an integrated pipeline across six phases:

```text
HTTP Request (POST /api/rag/query)  │  CLI Command (java -jar ... rag "<query>")
                    │                                 │
                    ▼                                 ▼
             RagServlet (Web Adapter)       Application (CLI Adapter)
                    │                                 │
                    └─────────────────┬───────────────┘
                                      ▼
                                  RagService (Application Core Orchestrator)
                                      │
               ┌──────────────────────┴──────────────────────┐
               ▼                                             ▼
    SimilaritySearchService                        ContextAssembler & PromptBuilder
               │                                             │
               ▼                                             ▼
    FileVectorStore (data/vectors.dat)             LlmGenerationProvider (Gemini REST API)
               │                                             │
               ▼                                             ▼
    RetrievedChunk[]                               Generated Answer String
               └──────────────────────┬──────────────────────┘
                                      ▼
                                 RagResponse (JSON / Console Output)
```

## Web Layer Architecture & Endpoints

### 1. Application Dependency Bootstrap (`WebContextListener`)
Uses `@WebListener` to implement `ServletContextListener`. Initializes `AppConfig`, `EmbeddingProvider`, `FileVectorStore`, `SimilaritySearchService`, `GeminiGenerationProvider`, and `RagService` once during Tomcat container startup, binding shared singletons to the `ServletContext`.

### 2. Health Endpoint (`HealthServlet`)
- **Mapping**: `GET /api/health`
- **Response Code**: `200 OK`
- **Content-Type**: `application/json; charset=UTF-8`
- **Response Body**:
  ```json
  {
    "status": "UP"
  }
  ```

### 3. RAG Query Endpoint (`RagServlet`)
- **Mapping**: `POST /api/rag/query`
- **Content-Type**: `application/json`
- **Request Payload (`RagQueryRequest`)**:
  ```json
  {
    "query": "What is the Maven build lifecycle?"
  }
  ```
- **Success Response Code**: `200 OK`
- **Response Body (`RagResponse`)**:
  ```json
  {
    "query": "What is the Maven build lifecycle?",
    "generatedAnswer": "The Maven build lifecycle is a defined sequence of phases...",
    "retrievedChunks": [
      {
        "chunk": {
          "id": "3276a633...",
          "documentId": "doc1",
          "sourcePath": "knowledge/maven/lifecycle.md",
          "chunkIndex": 0,
          "text": "The build lifecycle consists of validate, compile, test, package...",
          "contentLength": 120,
          "tokenCount": 20
        },
        "similarityScore": 0.8542
      }
    ],
    "sources": [
      {
        "documentId": "doc1",
        "sourcePath": "knowledge/maven/lifecycle.md",
        "snippet": "The build lifecycle consists of validate, compile, test, package...",
        "relevanceScore": 0.8542
      }
    ]
  }
  ```

### 4. HTTP Error Status Code Mapping

| Scenario | HTTP Status | Response JSON Body |
| :--- | :--- | :--- |
| Missing request body | `400 Bad Request` | `{"error": "Request body is required"}` |
| Missing query field (`{}`) | `400 Bad Request` | `{"error": "Query field must not be null"}` |
| Null query (`{"query": null}`) | `400 Bad Request` | `{"error": "Query field must not be null"}` |
| Blank query (`{"query": "   "}`) | `400 Bad Request` | `{"error": "Query must not be blank"}` |
| Malformed JSON syntax | `400 Bad Request` | `{"error": "Malformed JSON request"}` |
| Unsupported HTTP method (`GET`, `PUT`) | `405 Method Not Allowed` | `{"error": "Method Not Allowed"}` |
| Non-existent URL path | `404 Not Found` | Container 404 response |
| Internal / LLM service error | `500 Internal Server Error` | `{"error": "Sanitized error message"}` |

### 5. Security & Isolation Guarantee
- **Credential Protection**: `GEMINI_API_KEY` is transmitted solely via `x-goog-api-key` HTTP header and is never logged, written to properties, exposed in URL parameters, or included in error JSON.
- **Error Sanitization**: Error responses return sanitized JSON error messages and never expose raw stack traces, API keys, or internal filesystem paths to HTTP clients.
- **Thread Safety**: Servlets remain completely stateless; no request-specific state is stored in Servlet instance fields.

## Core Components & Pipeline

1. **Document Ingestion (`DocumentLoader`, `TextChunker`)**: Discovers Markdown/Text files, computes SHA-256 relative path IDs and content hashes, and chunks text deterministically with configurable window size and overlap.
2. **Vector Embeddings (`GeminiEmbeddingProvider`, `DummyEmbeddingProvider`)**: Generates L2-normalized 768-dimensional float vectors via Gemini REST API (`gemini-embedding-001`) or deterministic offline hashing.
3. **Vector Persistence (`FileVectorStore`)**: Appends records to custom binary file (`data/vectors.dat`) with magic header `0x4D4B4C56` (`MKLV`), rebuilding a memory offset index (`Map<String, Long>`) on startup.
4. **Similarity Retrieval (`SimilaritySearchService`, `CosineSimilarity`)**: Executes double-precision linear cosine similarity search with score threshold filtering (`0.7`) and deterministic tie-breaking.
5. **Context Assembly (`ContextAssembler`)**: Formats retrieved chunks into delimited, source-attributed context blocks.
6. **Grounded Prompting (`PromptBuilder`)**: Constructs deterministic system prompts instructing the LLM to answer strictly using supplied context and avoid inventing facts.
7. **RAG Orchestration (`RagService`)**: Combines embedding, search, context formatting, LLM generation, and source attribution into an immutable `RagResponse`. Short-circuits immediately when zero chunks qualify without calling the LLM.

## Configuration Properties

Configured via property files, JVM system properties, or environment variables:
- `knowledge.path`: `knowledge` (default)
- `data.path`: `data` (default)
- `vector.store.path`: `data/vectors.dat` (default)
- `retrieval.top-k`: `3` (default)
- `retrieval.min-similarity`: `0.7` (default)
- `embedding.provider`: `gemini` (default)
- `embedding.model`: `gemini-embedding-001` (default)
- `embedding.dimensions`: `768` (default)
- `embedding.timeout-seconds`: `30` (default)
- `generation.provider`: `gemini` (default)
- `generation.model`: `gemini-1.5-flash` (default)
- `generation.timeout-seconds`: `30` (default)

## Build, Test & Deployment Instructions

### Automated Tests
Run full offline unit and web test suite (119 total tests):
```cmd
.\mvnw.cmd clean test
```

Run opt-in live Gemini API integration test (requires `GEMINI_API_KEY`):
```cmd
.\mvnw.cmd test -Dgemini.integration=true
```

### Packaging WAR Artifact
Build production WAR artifact:
```cmd
.\mvnw.cmd clean package
```
Output artifact location: `target/maven-knowledge-lab-1.0-SNAPSHOT.war`

### Tomcat Deployment
1. Copy `target/maven-knowledge-lab-1.0-SNAPSHOT.war` into Apache Tomcat's `webapps/` directory.
2. Start Tomcat (`bin/startup.bat` or `bin/catalina.bat run`).
3. Access endpoints under context path `/maven-knowledge-lab-1.0-SNAPSHOT`:
   - `http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/api/health`
   - `http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/api/rag/query`

### Testing Endpoints via Curl

#### Health Check:
```bash
curl -X GET http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/api/health
```

#### RAG Query:
```bash
curl -X POST http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/api/rag/query \
     -H "Content-Type: application/json" \
     -d '{"query": "What is the Maven build lifecycle?"}'
```

### CLI Execution (Preserved Interface)
- Index documents into vector store:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar index
  ```
- Run RAG query via CLI:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar rag "What is the Maven build lifecycle?"
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
    │   │       ├── generation/
    │   │       │   ├── ContextAssembler.java
    │   │       │   ├── DummyLlmGenerationProvider.java
    │   │       │   ├── GeminiGenerationProvider.java
    │   │       │   ├── GenerationException.java
    │   │       │   ├── LlmGenerationProvider.java
    │   │       │   ├── PromptBuilder.java
    │   │       │   └── RagService.java
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
    │   │       ├── vector/
    │   │       │   ├── FileVectorStore.java
    │   │       │   ├── VectorStore.java
    │   │       │   └── VectorStoreException.java
    │   │       └── web/
    │   │           ├── HealthServlet.java
    │   │           ├── RagQueryRequest.java
    │   │           ├── RagServlet.java
    │   │           └── WebContextListener.java
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
                ├── generation/
                │   ├── ContextAssemblerTest.java
                │   ├── GeminiGenerationIntegrationTest.java
                │   ├── GeminiGenerationProviderTest.java
                │   ├── PromptBuilderTest.java
                │   └── RagServiceTest.java
                ├── model/
                │   └── DomainModelsTest.java
                ├── retrieval/
                │   ├── CosineSimilarityTest.java
                │   └── SimilaritySearchServiceTest.java
                ├── vector/
                │   ├── FileVectorStoreTest.java
                │   └── VectorStoreCorruptionTest.java
                └── web/
                    ├── HealthServletTest.java
                    ├── ManualWebVerificationTest.java
                    ├── MockHttpServletRequest.java
                    ├── MockHttpServletResponse.java
                    └── RagServletTest.java
```
