# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a production-quality educational Java 17 project demonstrating the full architectural evolution of a modern knowledge system: document ingestion, vector embeddings, local vector storage, similarity search, RAG answer generation, Jakarta Servlet web packaging, controlled agentic orchestration, Model Context Protocol (MCP) tool integration, and a unified browser-based developer console.

## System Progression (Phases 1 - 8)

```text
Phase 1  Maven Foundation
Phase 2  Document Ingestion + Deterministic Chunking
Phase 3  Embedding Provider (Gemini / Dummy)
Phase 4  Persistent Vector Store + Cosine Similarity Retrieval
Phase 5  RAG Answer Generation Pipeline
Phase 6  Servlet Web Interface (Jakarta Servlet 6.0 + WAR Packaging)
Phase 7  Knowledge Agent + Controlled Tools
Phase 8  MCP Protocol Lab + Unified Browser Developer Console
```

## System Architecture

```text
                         BROWSER DEVELOPER CONSOLE
                                     │
                                     ▼
                       ┌───────────────────────────┐
                       │ Developer Console Servlet │
                       │           GET /           │
                       └─────────────┬─────────────┘
                                     │ HTTP
                                     ▼
                              Servlet Layer
                                     │
              ┌──────────────────────┼──────────────────────┐
              ▼                      ▼                      ▼
         Knowledge API            RAG API               Agent API
   /api/knowledge/documents    /api/rag/query        /api/agent/query
              │                      │                      │
              ▼                      ▼                      ▼
       DocumentLoader            RagService           KnowledgeAgent
              │                      │                      │
              └──────────────────────┼──────────────────────┘
                                     │
                               Core RAG System
                                     │
                        ┌────────────┴────────────┐
                        ▼                         ▼
             SimilaritySearchService         VectorStore

────────────────────────────────────────────────────────────────────────────

                      SEPARATE MCP PROTOCOL LAB

                         McpKnowledgeClient
                                 │
                               STDIO
                                 │
                                 ▼
                         McpKnowledgeServer
                                 │
                                 ▼
                        getKnowledgeDocument
                                 │
                                 ▼
                         Knowledge Corpus
```

## Phase 8 Features

### 1. MCP Protocol Lab
- **SDK**: Official Java MCP SDK (`io.modelcontextprotocol.sdk:mcp-core:2.0.1` and `mcp-json-jackson2:2.0.1`).
- **Transport**: STDIO transport. STDOUT is reserved exclusively for MCP JSON-RPC protocol framing; all diagnostics are routed to STDERR.
- **MCP Tool (`getKnowledgeDocument`)**:
  - **Input Schema**: JSON Schema requiring string `path` (e.g. `java/collections.md`).
  - **Output**: Structured JSON object containing `path`, `title`, and `content`.
  - **Security Boundaries**: Path traversal (`..`), leading slashes (`/`), drive letters (`C:\`), and absolute paths are strictly rejected.
- **Client & Child Process Execution**: `McpKnowledgeClient` launches `McpKnowledgeServer` as a child process over STDIO, initializes protocol, discovers tools, executes `getKnowledgeDocument`, and terminates processes cleanly.

### 2. Unified Browser Developer Console
- **Location**: Access at `GET /` or `GET /console` after deploying WAR to Apache Tomcat (`http://localhost:8080/maven-knowledge-lab-1.0-SNAPSHOT/`).
- **Technology**: Single embedded HTML page using CSS and native Vanilla JavaScript (`fetch()`). Zero external frameworks (No React, Vite, Tailwind, Angular, or external CDN dependencies).
- **Console Sections**:
  1. **Knowledge**: Browse corpus documents, view metadata, and preview full file content (`GET /api/knowledge/documents`, `GET /api/knowledge/document`).
  2. **RAG**: Execute grounded RAG queries, view generated answers, and inspect retrieved source chunks (`POST /api/rag/query`).
  3. **Agent**: Run controlled knowledge agent, inspect final answers, and trace multi-step `observe-decide-act` iteration history (`POST /api/agent/query`).
  4. **MCP**: Execute request-scoped MCP test over STDIO transport, inspect discovered tools, invoke `getKnowledgeDocument`, and inspect raw structured response (`POST /api/mcp/test`).

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
- `generation.provider`: `gemini` (default)
- `generation.model`: `gemini-3.6-flash` (default)
- `generation.timeout-seconds`: `30` (default)

## Build, Test & Execution Commands

### Full Test Suite Execution
Run offline test suite (152 total tests across all 8 phases):
```cmd
.\mvnw.cmd clean test
```

### Packaging WAR Package
Build WAR package for Tomcat deployment:
```cmd
.\mvnw.cmd clean package
```
Output artifact: `target/maven-knowledge-lab-1.0-SNAPSHOT.war`

### CLI Execution Commands

- **MCP Protocol Lab Demonstration**:
  ```cmd
  java -cp "target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/classes;target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/lib/*" com.shevay.knowledge.Application mcp java/collections.md
  ```

- **Controlled Knowledge Agent**:
  ```cmd
  java -cp "target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/classes;target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/lib/*" com.shevay.knowledge.Application agent "Explain Maven dependency scopes"
  ```

- **Deterministic RAG Query**:
  ```cmd
  java -cp "target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/classes;target/maven-knowledge-lab-1.0-SNAPSHOT/WEB-INF/lib/*" com.shevay.knowledge.Application rag "What is the Maven lifecycle?"
  ```

## Explicit Scope Boundaries
To maintain educational focus and production purity, the following features are **intentionally NOT implemented**:
- No Spring / Spring Boot / Spring AI
- No Jackson 3 (unified on Jackson `2.17.0`)
- No React / Vite / Next.js / Tailwind (Developer Console uses native HTML/CSS/JS)
- No Database (PostgreSQL / pgVector / Redis)
- No Remote HTTP/SSE MCP transport (STDIO protocol focus)
- No arbitrary filesystem or shell tools (`ProcessBuilder` restricted to server launcher)
- No Phase 9
