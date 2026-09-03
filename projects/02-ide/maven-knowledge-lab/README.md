# Maven Knowledge Lab

## Project Purpose
Maven Knowledge Lab is a Java 17 Retrieval-Augmented Generation (RAG) learning system designed to demonstrate end-to-end document processing, vector embeddings, local vector storage, exact similarity retrieval, context construction, LLM-assisted answer generation, Jakarta Servlet web deployment, and controlled agentic orchestration over local knowledge repositories.

## Current Implementation Stage
Current Status: Phase 7 - Controlled Knowledge Agent + Tools

Phase 7 introduces a small, controlled agentic orchestration layer (`KnowledgeAgent`) operating above the frozen Phase 1-6 application core. The agent demonstrates the fundamental `observe -> decide -> act -> observe -> decide -> final_answer` execution loop. It operates strictly through allowlisted tools in `ToolRegistry` without general-purpose autonomous code execution or uncontrolled filesystem access.

## System Architecture

```text
                                User Input / CLI (agent "<query>")
                                               │
                                               ▼
                                         KnowledgeAgent (Bounded Iteration Loop)
                                               │
               ┌───────────────────────────────┴───────────────────────────────┐
               ▼                                                               ▼
    AgentDecisionProvider                                                 ToolRegistry
(GeminiAgentDecisionProvider / Dummy)                                  (Allowlisted Knowledge Tools)
               │                                                               │
               ▼                                       ┌───────────────────────┼───────────────────────┐
    AgentDecision Parsing & Validation                 ▼                       ▼                       ▼
 (FINAL_ANSWER | TOOL_CALL)                  SearchKnowledgeTool     GetDocumentTool     ExplainMavenConceptTool
                                                       │                       │                       │
                                                       ▼                       ▼                       ▼
                                            SimilaritySearchService     DocumentLoader            RagService
```

## Controlled Knowledge Agent

### RAG vs Controlled Agent Comparison

| Dimension | Deterministic RAG Pipeline (Phase 5) | Controlled Knowledge Agent (Phase 7) |
| :--- | :--- | :--- |
| **Orchestration** | Linear, single-pass pipeline (`Search -> Context -> Prompt -> LLM`) | Bounded multi-step `observe-decide-act` loop (Max 3 iterations) |
| **Tool Selection** | Fixed static retrieval and prompt flow | Dynamic model-selected tool call from allowlisted registry |
| **Execution Safety** | Pre-determined fixed execution path | Strict allowlist validation; unregistered tools & invalid args rejected |
| **Scope** | Document retrieval & answer synthesis | Multi-step reasoning over corpus documents & higher-level tools |

### Available Tools (`ToolRegistry`)

1. **`searchKnowledge` (`SearchKnowledgeTool`)**:
   - **Purpose**: Wraps `SimilaritySearchService` to perform exact vector similarity search over `FileVectorStore`.
   - **Arguments**: `{"query": "<search string>"}`

2. **`getDocument` (`GetDocumentTool`)**:
   - **Purpose**: Operates strictly on approved knowledge corpus documents (`knowledge/` directory).
   - **Arguments**: `{"documentPath": "<relative file path>"}`
   - **Security**: Rejects path traversal (`..`), leading slashes (`/`), drive prefixes (`C:\`), and unauthorized external paths.

3. **`explainMavenConcept` (`ExplainMavenConceptTool`)**:
   - **Purpose**: Higher-level knowledge tool delegating to `RagService` for grounded RAG answer generation.
   - **Arguments**: `{"concept": "<maven topic>"}`

### Decision Protocol & Validation

The agent decision provider parses model output into structured `AgentDecision` records:

- **Form 1 — Final Answer**:
  ```json
  {
    "type": "final_answer",
    "answer": "Maven build lifecycle consists of validate, compile, test, package, verify, install, and deploy."
  }
  ```

- **Form 2 — Tool Call**:
  ```json
  {
    "type": "tool_call",
    "tool": "searchKnowledge",
    "arguments": {
      "query": "Maven lifecycle phases"
    }
  }
  ```

Every LLM decision is treated as untrusted input. Malformed JSON, missing types, unknown tools, or invalid arguments are rejected cleanly.

### Security Boundaries
- **Allowlist Enforcement**: Only tools registered in `ToolRegistry` can execute; dynamic class loading or reflection is forbidden.
- **Path Traversal Protection**: `GetDocumentTool` validates paths and restricts reading strictly to local corpus documents.
- **No System Access**: Shell execution, `ProcessBuilder`, arbitrary file writes, network calls outside Gemini REST API, and arbitrary code execution are prohibited.
- **Credential Secrecy**: `GEMINI_API_KEY` is transmitted solely via `x-goog-api-key` HTTP header and is never logged, printed, or included in error messages.

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
- `generation.model`: `gemini-1.5-flash` (default)
- `generation.timeout-seconds`: `30` (default)

## Build, Test & CLI Commands

### Automated Tests
Run full offline test suite (143 total tests):
```cmd
.\mvnw.cmd clean test
```

Run opt-in live Gemini API integration tests (requires `GEMINI_API_KEY`):
```cmd
.\mvnw.cmd test -Dgemini.integration=true
```

### Packaging WAR Artifact
Build production WAR artifact:
```cmd
.\mvnw.cmd clean package
```
Output artifact location: `target/maven-knowledge-lab-1.0-SNAPSHOT.war`

### CLI Execution

- **Controlled Knowledge Agent**:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar agent "Explain Maven dependency scopes"
  ```

- **Deterministic RAG Pipeline**:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar rag "What is the Maven build lifecycle?"
  ```

- **Vector Search**:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar search "What is the Maven lifecycle?"
  ```

- **Index Knowledge Base**:
  ```cmd
  java -jar target/maven-knowledge-lab-1.0-SNAPSHOT.jar index
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
    │   │       ├── agent/
    │   │       │   ├── AgentContext.java
    │   │       │   ├── AgentDecision.java
    │   │       │   ├── AgentDecisionProvider.java
    │   │       │   ├── AgentDecisionType.java
    │   │       │   ├── AgentException.java
    │   │       │   ├── AgentExecutionEntry.java
    │   │       │   ├── AgentTool.java
    │   │       │   ├── DummyAgentDecisionProvider.java
    │   │       │   ├── GeminiAgentDecisionProvider.java
    │   │       │   ├── KnowledgeAgent.java
    │   │       │   ├── ToolCall.java
    │   │       │   ├── ToolRegistry.java
    │   │       │   ├── ToolResult.java
    │   │       │   └── tools/
    │   │       │       ├── ExplainMavenConceptTool.java
    │   │       │       ├── GetDocumentTool.java
    │   │       │       └── SearchKnowledgeTool.java
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
                ├── agent/
                │   ├── AgentDecisionParserTest.java
                │   ├── AgentToolsTest.java
                │   ├── GeminiAgentIntegrationTest.java
                │   ├── KnowledgeAgentTest.java
                │   └── ToolRegistryTest.java
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
