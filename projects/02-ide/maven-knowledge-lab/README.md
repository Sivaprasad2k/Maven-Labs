# Maven Knowledge Lab

Maven Knowledge Lab is a Java 17 and Apache Maven learning laboratory that progressively builds a full-stack Retrieval-Augmented Generation (RAG) system, a controlled Knowledge Agent, and a Model Context Protocol (MCP) tool integration layer. The project intentionally avoids high-level frameworks like Spring Boot or LangChain to expose the foundational software engineering mechanics of document chunking, vector embeddings, similarity search, HTTP transport, servlet handling, and agent decision loops.

## Overview

The primary objective of this repository is to demonstrate fundamental Java engineering and Apache Maven build concepts through the construction of an intelligent document retrieval platform. Instead of relying on black-box abstractions, every component—from text token chunking and binary vector file storage to HTTP request dispatching and MCP tool execution—is implemented directly using standard Java libraries and explicit architecture.

The laboratory progresses through a multi-tier learning roadmap:
1. Deterministic document parsing, identity hashing, and overlapping window chunking.
2. Embedding provider abstractions for local testing and Google Gemini API integration.
3. Custom binary vector store persistence and exact cosine similarity ranking.
4. RAG context assembly, prompt construction, and grounded text generation.
5. Jakarta Servlet web interface deployment on Apache Tomcat 10.
6. Multi-step agent orchestration with bounded iteration loops and tool validation.
7. Standardized Model Context Protocol (MCP) server over STDIO transport.
8. Observable developer console for real-time inspection of retrieval pipelines and agent traces.

## Architecture

The system follows a layered architecture where user interfaces (CLI, Web Servlets, and MCP Client) interact with underlying application services, which coordinate domain models, external Gemini providers, and binary storage.

```
+-----------------------------------------------------------------------+
|                           USER INTERFACES                             |
|  +--------------------+   +----------------------+   +-------------+  |
|  |   CLI Commands     |   |   Jakarta Servlets   |   | MCP Client  |  |
|  | (Application.java) |   | (Developer Console)  |   |   (STDIO)   |  |
|  +---------+----------+   +----------+-----------+   +------+------+  |
+------------|-------------------------|----------------------|---------+
             |                         |                      |
             v                         v                      v
+-----------------------------------------------------------------------+
|                         APPLICATION SERVICES                          |
|  +---------------------+  +--------------------+  +----------------+  |
|  | Document Ingestion  |  | Similarity Search  |  |  RagService    |  |
|  +---------------------+  +--------------------+  +----------------+  |
|  | KnowledgeAgent      |  | ToolRegistry       |  | McpServer      |  |
|  +---------------------+  +--------------------+  +----------------+  |
+------------|-------------------------|----------------------|---------+
             |                         |                      |
             v                         v                      v
+-----------------------------------------------------------------------+
|                          CORE INFRASTRUCTURE                          |
|  +---------------------+  +--------------------+  +----------------+  |
|  |  FileVectorStore    |  |  Gemini Providers  |  | Java HttpClient|  |
|  | (data/vectors.dat)  |  | (Embedding / LLM)  |  | (v1beta API)   |  |
|  +---------------------+  +--------------------+  +----------------+  |
+-----------------------------------------------------------------------+
```

### Component Flow
- **Ingestion & Indexing**: Raw Markdown files in `knowledge/` are discovered, normalized, chunked into overlapping windows, converted to 768-dimensional float vectors via `GeminiEmbeddingProvider`, and persisted to `data/vectors.dat`.
- **RAG Execution**: User queries generate query embeddings, search stored vectors via `SimilaritySearchService` using cosine similarity, assemble retrieved context, and submit prompts to `gemini-3.6-flash` via the Gemini Interactions API (`POST /v1beta/interactions`).
- **Agent Loop**: `KnowledgeAgent` uses `GeminiAgentDecisionProvider` to evaluate user intent, select tools from `ToolRegistry`, execute tools (`searchKnowledge`, `getDocument`, `explainMavenConcept`), and formulate grounded responses within bounded iterations.
- **MCP Integration**: `McpKnowledgeServer` exposes `getKnowledgeDocument` over STDIO transport, enforcing security boundary checks against directory traversal.

## Core Capabilities

### Document Ingestion
- **Discovery & Processing**: `DocumentIngestionService` recursively scans the `knowledge/` corpus, filtering for `.md` files and tracking ingestion statistics.
- **Content Identity & Hashing**: Each document is assigned a unique identifier (`doc-java-collections`) and SHA-256 content hash (`contentHash`) to detect modifications.
- **Deterministic Chunking**: `TextChunker` breaks documents into sliding windows (default `chunkSize = 800` characters, `overlap = 100` characters), creating immutable `DocumentChunk` instances with chunk indices and position metadata.

### Embedding
- **Provider Abstraction**: `EmbeddingProvider` defines the contract for converting text into float array vectors.
- **Gemini Embedding Provider**: `GeminiEmbeddingProvider` communicates directly with Google Gemini's REST API using Java 17 `HttpClient`, requesting 768-dimensional vectors using `models/gemini-embedding-001`.
- **Embedding Purpose Distinction**: Differentiates between `EmbeddingPurpose.DOCUMENT` (indexing) and `EmbeddingPurpose.QUERY` (retrieval) to align with embedding model task types.
- **Dummy Fallback Provider**: `DummyEmbeddingProvider` produces deterministic pseudo-embeddings for hermetic unit testing when `GEMINI_API_KEY` is not present.

### Vector Storage and Retrieval
- **FileVectorStore**: A custom binary vector storage engine that serializes `VectorRecord` objects to `data/vectors.dat` using DataOutputStream and DataInputStream primitives.
- **Exact Cosine Similarity**: `SimilaritySearchService` calculates exact dot-product cosine similarity between query vectors and stored document vectors without relying on third-party vector databases.
- **Deterministic Filtering & Ranking**: Results are filtered against a minimum similarity threshold (`minSimilarity = 0.70`), sorted by score descending, and capped at `topK = 3`.

### Retrieval-Augmented Generation
- **Pipeline Stages**: Query $\rightarrow$ Query Embedding $\rightarrow$ Exact Cosine Search $\rightarrow$ Context Assembly $\rightarrow$ Prompt Construction $\rightarrow$ Generation Provider.
- **Grounded Safeguards**: `RagService` validates retrieved context against the minimum similarity threshold. If no chunks pass the threshold, the pipeline returns a explicit message stating no relevant knowledge context was found, preventing hallucinated answers.
- **Generation Model**: Text generation uses `gemini-3.6-flash` via `POST https://generativelanguage.googleapis.com/v1beta/interactions` with `x-goog-api-key` header authentication.

### Servlet Web Interface
- **Jakarta Servlet 6.0**: Web endpoints are implemented using standard Jakarta Servlets mapped via annotations.
- **Endpoint Structure**:
  - `GET /`, `GET /console`: Renders the Developer Console (`DeveloperConsoleServlet`).
  - `POST /api/rag/query`: RAG pipeline query execution (`RagServlet`).
  - `POST /api/agent/query`: Controlled agent decision loop execution (`AgentServlet`).
  - `GET /api/knowledge/documents`, `GET /api/knowledge/document`: Corpus listing and document fetching (`KnowledgeServlet`).
  - `GET /api/mcp`, `POST /api/mcp/test`: MCP tool invocation and STDIO verification (`McpServlet`).
  - `GET /api/health`: Health status endpoint (`HealthServlet`).
- **Context Initialization**: `WebContextListener` inspects JVM system properties and environment variables during deployment to wire dependencies (`GeminiEmbeddingProvider`, `GeminiGenerationProvider`, `FileVectorStore`).

### Controlled Knowledge Agent
- **Orchestration Architecture**: `KnowledgeAgent` manages a bounded loop (maximum `5` iterations) that delegates decisions to `AgentDecisionProvider`.
- **Tool Registry**: `ToolRegistry` maintains registered tool instances (`SearchKnowledgeTool`, `GetDocumentTool`, `ExplainMavenConceptTool`).
- **Decision Engine**: `GeminiAgentDecisionProvider` submits query history and available tool definitions to `gemini-3.6-flash`, parsing structured JSON decisions (`TOOL_CALL` or `FINAL_ANSWER`).
- **Execution Boundaries**: Tools are validated against an explicit allowlist. The agent cannot execute system commands, access unauthorized files, or perform network calls outside its registered tools.

### MCP Protocol Lab
- **Isolated Design**: Model Context Protocol (MCP) is implemented as a standalone protocol demonstration independent of the internal `KnowledgeAgent`.
- **STDIO Transport**: `McpKnowledgeServer` exposes knowledge tools over standard input/output streams using `mcp-core` SDK.
- **Tool Definition**: Implements `getKnowledgeDocument`, accepting relative document paths (`java/collections.md`).
- **Path Traversal Protection**: Enforces security boundaries, rejecting path traversal attempts (e.g. `../pom.xml`) with HTTP 400 / access denied errors.

### Developer Console
- **Single-Page Inspection Interface**: Served directly by `DeveloperConsoleServlet` using native HTML, Vanilla CSS, and JavaScript.
- **Observability Views**:
  - **Knowledge Base**: Metric cards (`Document Count`, `Vector Records`, `Dimensions`), document tree, and raw markdown reader.
  - **RAG Pipeline**: Query input stage, generated answer panel, raw cosine similarity scores (`0.8842`), and chunk previews.
  - **Agent Trace**: Vertical execution timeline detailing step number, decision type, tool name, arguments, and execution outputs.
  - **MCP Inspector**: Protocol status, tool JSON schema viewer, interactive path invocation form, and raw output JSON viewer.

## Technology Stack

| Layer / System | Technology / Library | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 17 | Core runtime environment |
| **Build System** | Apache Maven | 3.9+ (Wrapper) | Dependency management, compilation, packaging |
| **Web Runtime** | Jakarta Servlet | 6.0.0 | HTTP endpoint dispatching |
| **Servlet Container** | Apache Tomcat | 10.1.x | Web application container |
| **JSON Parser** | Jackson Databind | 2.17.0 | Data serialization & API JSON parsing |
| **Protocol Integration**| MCP Core SDK | 2.0.1 | Model Context Protocol STDIO implementation |
| **HTTP Transport** | Java `HttpClient` | Built-in (JDK 17) | Direct REST communication with Gemini APIs |
| **Testing Framework** | JUnit Jupiter | 5.10.2 | Unit, service, and servlet test execution |
| **Vector Store** | Custom Binary (`DataOutputStream`) | Built-in | Local vector file persistence (`data/vectors.dat`) |

## Maven Concepts Demonstrated

As a Maven learning laboratory, this repository demonstrates key build engineering concepts:

- **Maven Coordinates**: Standardized `groupId` (`com.shevay.knowledge`), `artifactId` (`maven-knowledge-lab`), and `version` (`1.0-SNAPSHOT`) structure.
- **Dependency Management & Scopes**: Explicit scope declarations (`provided` for `jakarta.servlet-api`, `test` for `junit-jupiter`).
- **WAR Packaging**: Configured `<packaging>war</packaging>` with `maven-war-plugin` web resource mapping (`knowledge/` and `data/` copied into the WAR archive).
- **Executable Manifest Wiring**: `maven-jar-plugin` configuration defining `Main-Class` (`com.shevay.knowledge.Application`) for standalone CLI execution.
- **Compiler Configuration**: `maven-compiler-plugin` setting Java 17 release target (`<release>17</release>`).
- **Test Automation**: `maven-surefire-plugin` executing test suites during `mvnw clean test`.
- **Hermetic Build Wrapper**: Bundled `mvnw` / `mvnw.cmd` scripts ensuring consistent Maven version usage without pre-installed local tooling.

## Java Engineering Concepts

- **Interface Abstractions**: Decoupled contracts (`EmbeddingProvider`, `VectorStore`, `LlmGenerationProvider`, `AgentDecisionProvider`).
- **Immutable Domain Records**: Java 17 `record` types (`DocumentChunk`, `VectorRecord`, `RetrievedChunk`, `SourceReference`, `RagResponse`).
- **Robust Exception Hierarchy**: Specialized checked/unchecked exceptions (`VectorStoreException`, `EmbeddingException`, `GenerationException`).
- **Standard HTTP Communication**: Asynchronous and synchronous `java.net.http.HttpClient` utilization for JSON REST APIs without SDK dependencies.
- **Binary I/O Streams**: Primitive binary serialization using `DataOutputStream` and `DataInputStream` for compact vector storage.
- **Thread-Safe Servlets**: Stateless servlet request handlers communicating through thread-safe services.

## Project Structure

```
maven-knowledge-lab/
├── .mvn/                         # Maven Wrapper binary configuration
├── data/                         # Local vector persistence directory
│   └── vectors.dat               # Binary file store containing vector records
├── knowledge/                    # Knowledge corpus Markdown files
│   ├── java/                     # Java documentation (collections.md, concurrency.md)
│   └── maven/                    # Maven documentation (dependencies.md, lifecycle.md)
├── src/
│   ├── main/
│   │   └── java/com/shevay/knowledge/
│   │       ├── agent/            # Agent orchestration, decision providers, and tools
│   │       ├── config/           # Centralized configuration management (AppConfig)
│   │       ├── document/         # Ingestion, loader, content hashing, and text chunker
│   │       ├── embedding/        # Gemini and Dummy embedding providers
│   │       ├── generation/       # Gemini generation provider and RAG pipeline
│   │       ├── mcp/              # MCP Server, Client, and Test Service implementations
│   │       ├── model/            # Domain models and Java 17 records
│   │       ├── retrieval/        # Cosine similarity calculation and search service
│   │       ├── vector/           # Binary FileVectorStore implementation
│   │       └── web/              # Jakarta Servlets and Developer Console HTML
│   └── test/
│       └── java/com/shevay/knowledge/ # Hermetic unit and integration test suite
├── mvnw                          # Linux/macOS Maven Wrapper script
├── mvnw.cmd                      # Windows Maven Wrapper script
├── pom.xml                       # Project Object Model configuration
└── README.md                     # Engineering platform documentation
```

## Running the Project

### Automated Tests
Run the automated test suite using the Maven Wrapper:
```bash
./mvnw clean test
```
*Windows (PowerShell/CMD):*
```cmd
.\mvnw.cmd clean test
```

### Build and Package
Package the project into a WAR file:
```cmd
.\mvnw.cmd clean package
```
The output WAR is generated at `target/maven-knowledge-lab-1.0-SNAPSHOT.war`.

### Command Line Interface
Run CLI sub-commands using Maven or java:
```cmd
# Document Ingestion
.\mvnw.cmd exec:java -Dexec.args="ingest"

# Vector Indexing (processes knowledge/ and saves to data/vectors.dat)
.\mvnw.cmd exec:java -Dexec.args="index"

# Similarity Search
.\mvnw.cmd exec:java -Dexec.args="search \"What is the Maven lifecycle?\""

# RAG Pipeline Query
.\mvnw.cmd exec:java -Dexec.args="rag \"What is the Maven lifecycle?\""

# Knowledge Agent Query
.\mvnw.cmd exec:java -Dexec.args="agent \"Explain Maven dependency scopes\""

# MCP Protocol Demo
.\mvnw.cmd exec:java -Dexec.args="mcp java/collections.md"
```

### Web Application (Tomcat Deployment)
1. Deploy `target/maven-knowledge-lab-1.0-SNAPSHOT.war` to Apache Tomcat 10 (`webapps/`).
2. Start Tomcat container.
3. Access the Developer Console in your browser:
   `http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/`
4. Health check endpoint:
   `http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/api/health`

## Configuration

Configuration parameters are managed by `AppConfig` and can be customized via JVM system properties or environment variables:

| Property / Parameter | Environment Variable | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `gemini.api.key` | `GEMINI_API_KEY` | `""` (Empty) | API key for Google Gemini REST services |
| `embedding.provider` | `EMBEDDING_PROVIDER` | `gemini` | Embedding provider (`gemini` or `dummy`) |
| `embedding.model` | `EMBEDDING_MODEL` | `gemini-embedding-001` | Gemini embedding model name |
| `embedding.dimensions`| `EMBEDDING_DIMENSIONS`| `768` | Target vector dimensionality |
| `generation.model` | `GENERATION_MODEL` | `gemini-3.6-flash` | Gemini text generation model name |
| `retrieval.top-k` | `RETRIEVAL_TOP_K` | `3` | Maximum retrieved vector results |
| `retrieval.min-similarity` | `MIN_SIMILARITY` | `0.70` | Cosine similarity cutoff threshold |

*Resolution Precedence for Gemini API Key*:
1. JVM System Property (`-Dgemini.api.key=...`)
2. Environment Variable (`GEMINI_API_KEY=...`)
3. Fallback to `null` (triggers safe `DummyEmbeddingProvider` fallback for offline testing)

*Security Warning*: Never commit `GEMINI_API_KEY` or pass API keys in source code or tracked configuration files.

## Automated Testing Strategy

The repository includes a comprehensive test suite of **154 automated tests**:
- **Unit Tests**: Test core domain records, chunking calculations, SHA-256 content hashing, cosine similarity algorithms, and prompt formatting.
- **Provider Tests**: Verify mock HTTP responses, request headers (`x-goog-api-key`), JSON payload structures, and error handling for Gemini REST APIs.
- **Servlet Tests**: Inspect HTTP status codes, headers, and JSON responses using `MockHttpServletRequest` and `MockHttpServletResponse`.
- **MCP Tests**: Test MCP server discovery, tool invocation, and path traversal rejection (`../pom.xml`).
- **Integration Tests**: Opt-in integration tests (`GeminiIntegrationTest`, `GeminiGenerationIntegrationTest`) execute against live Gemini APIs when `GEMINI_API_KEY` is present.

## Security and Design Boundaries

- **Credential Isolation**: API keys are read strictly from environment variables; zero credentials are stored in code or repository files.
- **Path Traversal Defense**: Knowledge file readers resolve paths against the canonical `knowledge/` root directory, rejecting attempts containing `../` or absolute directory references.
- **Bounded Agent Execution**: The `KnowledgeAgent` enforces a strict iteration limit (`5` steps) to prevent infinite decision loops.
- **Allowlisted Tool Execution**: Agents and MCP servers can only execute explicitly registered tools. Arbitrary command execution, filesystem access, or external network requests are strictly forbidden.

## Deliberate Design Constraints

To maintain a focused learning environment, the project intentionally excludes:
- **Framework Magic**: Excludes Spring, Spring Boot, and Spring AI to emphasize explicit Java dependency wiring.
- **Orchestration Libraries**: Excludes LangChain, LangGraph, or LlamaIndex to build RAG and agent loops from basic principles.
- **External Vector Databases**: Excludes PostgreSQL/pgvector, Pinecone, or Milvus in favor of a readable local binary vector store.
- **Complex Transports**: Excludes SSE/HTTP transports for MCP to focus on pure STDIO protocol mechanics.

## Engineering Trade-offs

- **File-Based Vector Persistence**: Chosen for simplicity and zero external infrastructure dependencies. Not intended for multi-million vector distributed scale.
- **Exact Cosine Search**: Iterates over all stored vectors in $O(N)$ time. Provides exact retrieval accuracy without approximate nearest neighbor (ANN) indexing overhead.
- **Native Java HttpClient**: Avoids heavy SDK dependencies while providing full control over HTTP headers, timeouts, and JSON payloads.
- **Manual Dependency Wiring**: Servlets and CLI commands manually instantiate required services, providing explicit lifecycle clarity over dependency injection containers.

## Project Status

Maven Knowledge Lab is a **completed engineering platform**. All core retrieval, generation, agent, web servlet, and MCP features are fully implemented, tested, and verified.
