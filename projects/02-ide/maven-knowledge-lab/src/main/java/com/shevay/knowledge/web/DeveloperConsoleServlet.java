package com.shevay.knowledge.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet serving the unified Developer Console at GET / and GET /console.
 * Designed with a minimalist light-first developer tool aesthetic for internal observability.
 */
@WebServlet(urlPatterns = {"", "/", "/console"})
public class DeveloperConsoleServlet extends HttpServlet {

    private static final String CONSOLE_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Maven Knowledge Lab — Developer Console</title>
              <style>
                :root {
                  --bg-main: #f8f9fa;
                  --surface-card: #ffffff;
                  --surface-subtle: #f1f3f5;
                  --border-color: #e9ecef;
                  --border-dark: #dee2e6;
                  --text-primary: #111827;
                  --text-muted: #6b7280;
                  --text-code: #0f172a;
                  --accent: #0969da;
                  --accent-hover: #0353b4;
                  --accent-light: rgba(9, 105, 218, 0.06);
                  --success: #16a34a;
                  --success-bg: rgba(22, 163, 74, 0.08);
                  --error: #dc2626;
                  --error-bg: rgba(220, 38, 38, 0.08);
                  --warning: #d97706;
                  --warning-bg: rgba(217, 119, 6, 0.08);
                  --mono-font: "JetBrains Mono", "IBM Plex Mono", SFMono-Regular, Consolas, monospace;
                  --sans-font: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                  --radius: 10px;
                  --shadow-sm: 0 1px 3px rgba(0,0,0,0.05);
                }

                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  font-family: var(--sans-font);
                  background-color: var(--bg-main);
                  color: var(--text-primary);
                  line-height: 1.5;
                  font-size: 0.9rem;
                  -webkit-font-smoothing: antialiased;
                }

                /* Layout Grid */
                .app-layout {
                  display: flex;
                  min-height: 100vh;
                }

                /* Sidebar */
                .sidebar {
                  width: 240px;
                  background-color: #ffffff;
                  border-right: 1px solid var(--border-color);
                  padding: 24px 16px;
                  display: flex;
                  flex-direction: column;
                  flex-shrink: 0;
                }

                .brand-header {
                  padding-bottom: 20px;
                  margin-bottom: 20px;
                  border-bottom: 1px solid var(--border-color);
                }

                .brand-title {
                  font-size: 0.95rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  display: flex;
                  align-items: center;
                  gap: 8px;
                }

                .brand-badge {
                  font-size: 0.65rem;
                  background-color: var(--accent-light);
                  color: var(--accent);
                  padding: 2px 6px;
                  border-radius: 4px;
                  font-weight: 600;
                }

                .brand-subtitle {
                  font-size: 0.75rem;
                  color: var(--text-muted);
                  margin-top: 4px;
                }

                .nav-section-title {
                  font-size: 0.7rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.08em;
                  color: var(--text-muted);
                  margin-bottom: 12px;
                  padding-left: 8px;
                }

                .nav-menu {
                  list-style: none;
                  display: flex;
                  flex-direction: column;
                  gap: 4px;
                }

                .tab-btn {
                  width: 100%;
                  text-align: left;
                  background: none;
                  border: 1px solid transparent;
                  color: var(--text-muted);
                  padding: 8px 12px;
                  border-radius: 6px;
                  font-size: 0.85rem;
                  font-weight: 500;
                  cursor: pointer;
                  display: flex;
                  align-items: center;
                  gap: 10px;
                  transition: all 0.15s ease;
                }

                .tab-btn:hover {
                  background-color: var(--surface-subtle);
                  color: var(--text-primary);
                }

                .tab-btn.active {
                  background-color: var(--accent-light);
                  color: var(--accent);
                  border-color: rgba(9, 105, 218, 0.15);
                  font-weight: 600;
                }

                .tab-btn:focus-visible {
                  outline: 2px solid var(--accent);
                }

                /* Main Stage */
                .main-stage {
                  flex: 1;
                  padding: 32px;
                  overflow-y: auto;
                  max-width: 1200px;
                }

                /* Header Toolbar */
                .top-bar {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 32px;
                  background: rgba(255, 255, 255, 0.85);
                  backdrop-filter: blur(8px);
                  padding: 16px 20px;
                  border-radius: var(--radius);
                  border: 1px solid var(--border-color);
                  box-shadow: var(--shadow-sm);
                }

                .page-heading {
                  font-size: 1.1rem;
                  font-weight: 700;
                  color: var(--text-primary);
                }

                .status-group {
                  display: flex;
                  align-items: center;
                  gap: 16px;
                  font-size: 0.75rem;
                  font-family: var(--mono-font);
                  color: var(--text-muted);
                }

                .status-item {
                  display: flex;
                  align-items: center;
                  gap: 6px;
                  background-color: var(--surface-subtle);
                  padding: 4px 10px;
                  border-radius: 20px;
                  border: 1px solid var(--border-color);
                }

                .dot {
                  width: 7px;
                  height: 7px;
                  border-radius: 50%;
                }

                .dot-success { background-color: var(--success); }
                .dot-warning { background-color: var(--warning); }

                /* Tab View Panes */
                .tab-content { display: none; }
                .tab-content.active { display: block; }

                /* Card Panel System */
                .card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 24px;
                  margin-bottom: 24px;
                  box-shadow: var(--shadow-sm);
                }

                .card-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 18px;
                  padding-bottom: 12px;
                  border-bottom: 1px solid var(--border-color);
                }

                .card-title {
                  font-size: 0.9rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.04em;
                  color: var(--text-muted);
                  display: flex;
                  align-items: center;
                  gap: 8px;
                }

                /* Metric Grid */
                .metrics-grid {
                  display: grid;
                  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                  gap: 16px;
                  margin-bottom: 24px;
                }

                .metric-card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 16px;
                  box-shadow: var(--shadow-sm);
                }

                .metric-label {
                  font-size: 0.75rem;
                  font-weight: 600;
                  color: var(--text-muted);
                  text-transform: uppercase;
                  letter-spacing: 0.03em;
                  margin-bottom: 6px;
                }

                .metric-val {
                  font-size: 1.25rem;
                  font-weight: 700;
                  font-family: var(--mono-font);
                  color: var(--text-primary);
                }

                .metric-sub {
                  font-size: 0.75rem;
                  color: var(--text-muted);
                  margin-top: 4px;
                }

                /* Forms & Inputs */
                .form-group {
                  margin-bottom: 16px;
                }

                label {
                  display: block;
                  font-size: 0.75rem;
                  font-weight: 600;
                  text-transform: uppercase;
                  letter-spacing: 0.04em;
                  color: var(--text-muted);
                  margin-bottom: 6px;
                }

                input[type="text"], textarea {
                  width: 100%;
                  padding: 10px 14px;
                  font-size: 0.875rem;
                  font-family: var(--sans-font);
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-dark);
                  border-radius: 6px;
                  color: var(--text-primary);
                  transition: border-color 0.15s ease;
                }

                input[type="text"]:focus, textarea:focus {
                  outline: none;
                  border-color: var(--accent);
                  box-shadow: 0 0 0 3px var(--accent-light);
                }

                .code-input {
                  font-family: var(--mono-font) !important;
                }

                .btn {
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  gap: 8px;
                  padding: 9px 16px;
                  font-size: 0.85rem;
                  font-weight: 600;
                  border-radius: 6px;
                  border: 1px solid transparent;
                  cursor: pointer;
                  transition: all 0.15s ease;
                }

                .btn-primary {
                  background-color: var(--accent);
                  color: #ffffff;
                }

                .btn-primary:hover {
                  background-color: var(--accent-hover);
                }

                .btn-secondary {
                  background-color: var(--surface-card);
                  border-color: var(--border-dark);
                  color: var(--text-primary);
                }

                .btn-secondary:hover {
                  background-color: var(--surface-subtle);
                }

                .btn:disabled {
                  opacity: 0.6;
                  cursor: not-allowed;
                }

                /* Data Tables / Code Blocks */
                .code-box {
                  background-color: var(--surface-subtle);
                  border: 1px solid var(--border-color);
                  border-radius: 6px;
                  padding: 16px;
                  font-family: var(--mono-font);
                  font-size: 0.825rem;
                  color: var(--text-code);
                  overflow-x: auto;
                  white-space: pre-wrap;
                  word-break: break-word;
                }

                .doc-list {
                  list-style: none;
                  display: flex;
                  flex-direction: column;
                  gap: 8px;
                }

                .doc-item {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: 6px;
                  padding: 12px 16px;
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  cursor: pointer;
                  transition: border-color 0.15s ease;
                }

                .doc-item:hover {
                  border-color: var(--accent);
                  background-color: var(--accent-light);
                }

                .doc-path {
                  font-family: var(--mono-font);
                  font-size: 0.85rem;
                  color: var(--text-primary);
                  font-weight: 500;
                }

                .badge {
                  font-size: 0.7rem;
                  font-family: var(--mono-font);
                  font-weight: 600;
                  padding: 2px 8px;
                  border-radius: 4px;
                  border: 1px solid transparent;
                }

                .badge-success {
                  background-color: var(--success-bg);
                  color: var(--success);
                  border-color: rgba(22, 163, 74, 0.2);
                }

                .badge-error {
                  background-color: var(--error-bg);
                  color: var(--error);
                  border-color: rgba(220, 38, 38, 0.2);
                }

                .badge-neutral {
                  background-color: var(--surface-subtle);
                  color: var(--text-muted);
                  border-color: var(--border-color);
                }

                /* Execution Trace Timeline */
                .trace-timeline {
                  display: flex;
                  flex-direction: column;
                  gap: 16px;
                  margin-top: 16px;
                  position: relative;
                  padding-left: 20px;
                  border-left: 2px solid var(--border-color);
                }

                .trace-step {
                  position: relative;
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: 6px;
                  padding: 14px 16px;
                }

                .trace-step::before {
                  content: '';
                  position: absolute;
                  left: -27px;
                  top: 18px;
                  width: 10px;
                  height: 10px;
                  border-radius: 50%;
                  background-color: var(--accent);
                  border: 2px solid #ffffff;
                }

                .trace-step-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 8px;
                }

                .trace-step-type {
                  font-family: var(--mono-font);
                  font-weight: 700;
                  font-size: 0.8rem;
                  color: var(--text-primary);
                }

                .banner-error {
                  background-color: var(--error-bg);
                  border: 1px solid rgba(220, 38, 38, 0.2);
                  color: var(--error);
                  padding: 12px 16px;
                  border-radius: 6px;
                  font-size: 0.85rem;
                  margin-bottom: 16px;
                  display: none;
                }

                /* Responsive */
                @media (max-width: 768px) {
                  .app-layout { flex-direction: column; }
                  .sidebar { width: 100%; border-right: none; border-bottom: 1px solid var(--border-color); }
                  .main-stage { padding: 16px; }
                  .top-bar { flex-direction: column; align-items: flex-start; gap: 12px; }
                }
              </style>
            </head>
            <body>
              <div class="app-layout">
                <!-- Sidebar Navigation -->
                <aside class="sidebar">
                  <div class="brand-header">
                    <div class="brand-title">
                      Maven Knowledge Lab
                      <span class="brand-badge">PHASE 8</span>
                    </div>
                    <div class="brand-subtitle">Developer Observability Console</div>
                  </div>

                  <div class="nav-section-title">Workspace</div>
                  <ul class="nav-menu">
                    <li>
                      <button class="tab-btn active" onclick="switchTab('knowledge')">
                        📄 Knowledge Base
                      </button>
                    </li>
                    <li>
                      <button class="tab-btn" onclick="switchTab('rag')">
                        🔍 RAG Pipeline
                      </button>
                    </li>
                    <li>
                      <button class="tab-btn" onclick="switchTab('agent')">
                        ⚡ Knowledge Agent
                      </button>
                    </li>
                    <li>
                      <button class="tab-btn" onclick="switchTab('mcp')">
                        🔌 MCP Protocol
                      </button>
                    </li>
                  </ul>
                </aside>

                <!-- Main Content Stage -->
                <main class="main-stage">
                  <!-- Top Bar Status Bar -->
                  <div class="top-bar">
                    <div class="page-heading" id="current-view-title">Knowledge Base Inspector</div>
                    <div class="status-group">
                      <div class="status-item">
                        <span class="dot dot-success"></span>
                        Runtime: Active
                      </div>
                      <div class="status-item">
                        <span class="dot dot-success"></span>
                        Model: gemini-3.6-flash
                      </div>
                      <div class="status-item">
                        <span class="dot dot-success"></span>
                        Vectors: 768-dim
                      </div>
                      <div class="status-item">
                        <span class="dot dot-success"></span>
                        MCP: STDIO
                      </div>
                    </div>
                  </div>

                  <!-- TAB 1: KNOWLEDGE BASE VIEW -->
                  <div id="tab-knowledge" class="tab-content active">
                    <div class="metrics-grid">
                      <div class="metric-card">
                        <div class="metric-label">Indexed Documents</div>
                        <div class="metric-val" id="stat-doc-count">4</div>
                        <div class="metric-sub">Markdown technical docs</div>
                      </div>
                      <div class="metric-card">
                        <div class="metric-label">Vector Store Records</div>
                        <div class="metric-val" id="stat-vector-count">5</div>
                        <div class="metric-sub">Persisted in vectors.dat</div>
                      </div>
                      <div class="metric-card">
                        <div class="metric-label">Vector Dimension</div>
                        <div class="metric-val">768</div>
                        <div class="metric-sub">gemini-embedding-001</div>
                      </div>
                    </div>

                    <div class="card">
                      <div class="card-header">
                        <div class="card-title">Indexed Document Corpus</div>
                        <button class="btn btn-secondary" onclick="loadKnowledgeDocs()">
                          Refresh List
                        </button>
                      </div>
                      <div id="doc-list-container">
                        <ul class="doc-list" id="doc-list-ul">
                          <li class="doc-item" onclick="viewDocDetails('java/collections.md')">
                            <span class="doc-path">java/collections.md</span>
                            <span class="badge badge-neutral">doc-java-collections</span>
                          </li>
                          <li class="doc-item" onclick="viewDocDetails('java/concurrency.md')">
                            <span class="doc-path">java/concurrency.md</span>
                            <span class="badge badge-neutral">doc-java-concurrency</span>
                          </li>
                          <li class="doc-item" onclick="viewDocDetails('maven/dependencies.md')">
                            <span class="doc-path">maven/dependencies.md</span>
                            <span class="badge badge-neutral">doc-maven-dependencies</span>
                          </li>
                          <li class="doc-item" onclick="viewDocDetails('maven/lifecycle.md')">
                            <span class="doc-path">maven/lifecycle.md</span>
                            <span class="badge badge-neutral">doc-maven-lifecycle</span>
                          </li>
                        </ul>
                      </div>
                    </div>

                    <div class="card" id="doc-detail-card" style="display: none;">
                      <div class="card-header">
                        <div class="card-title" id="doc-detail-title">Document Details</div>
                      </div>
                      <div id="doc-detail-content" class="code-box">Select a document to inspect payload.</div>
                    </div>
                  </div>

                  <!-- TAB 2: RAG PIPELINE VIEW -->
                  <div id="tab-rag" class="tab-content">
                    <div class="card">
                      <div class="card-header">
                        <div class="card-title">RAG Retrieval & Generation Execution</div>
                      </div>
                      <div class="form-group">
                        <label for="rag-input-query">Enter Search Query</label>
                        <input type="text" id="rag-input-query" placeholder="e.g. What is the Maven lifecycle?" value="What is the Maven lifecycle?">
                      </div>
                      <button class="btn btn-primary" id="rag-submit-btn" onclick="executeRagPipeline()">
                        Execute RAG Pipeline
                      </button>
                    </div>

                    <div class="banner-error" id="rag-error-banner"></div>

                    <div id="rag-result-stage" style="display: none;">
                      <div class="card">
                        <div class="card-header">
                          <div class="card-title">Generated Answer (gemini-3.6-flash)</div>
                        </div>
                        <div class="code-box" id="rag-answer-output">Awaiting generation...</div>
                      </div>

                      <div class="card">
                        <div class="card-header">
                          <div class="card-title">Retrieved Context & Similarity Scores</div>
                        </div>
                        <div id="rag-retrieval-container">No retrieval results.</div>
                      </div>
                    </div>
                  </div>

                  <!-- TAB 3: AGENT EXECUTION TRACE VIEW -->
                  <div id="tab-agent" class="tab-content">
                    <div class="card">
                      <div class="card-header">
                        <div class="card-title">Controlled Agent Execution Console</div>
                      </div>
                      <div class="form-group">
                        <label for="agent-input-query">Agent Goal / Question</label>
                        <input type="text" id="agent-input-query" placeholder="e.g. Explain Maven dependency scopes" value="Explain Maven dependency scopes">
                      </div>
                      <button class="btn btn-primary" id="agent-submit-btn" onclick="executeAgentLoop()">
                        Run Agent Decision Loop
                      </button>
                    </div>

                    <div class="banner-error" id="agent-error-banner"></div>

                    <div id="agent-result-stage" style="display: none;">
                      <div class="card">
                        <div class="card-header">
                          <div class="card-title">Agent Execution Step Trace</div>
                        </div>
                        <div class="trace-timeline" id="agent-trace-timeline"></div>
                      </div>

                      <div class="card">
                        <div class="card-header">
                          <div class="card-title">Final Agent Output</div>
                        </div>
                        <div class="code-box" id="agent-final-answer">Awaiting decision loop...</div>
                      </div>
                    </div>
                  </div>

                  <!-- TAB 4: MCP PROTOCOL INSPECTOR VIEW -->
                  <div id="tab-mcp" class="tab-content">
                    <div class="metrics-grid">
                      <div class="metric-card">
                        <div class="metric-label">MCP Status</div>
                        <div class="metric-val" style="color: var(--success);">CONNECTED</div>
                        <div class="metric-sub">STDIO Transport</div>
                      </div>
                      <div class="metric-card">
                        <div class="metric-label">Server Class</div>
                        <div class="metric-val" style="font-size: 0.95rem;">McpKnowledgeServer</div>
                        <div class="metric-sub">Phase 8 Protocol Lab</div>
                      </div>
                    </div>

                    <div class="card">
                      <div class="card-header">
                        <div class="card-title">Available Tool Schema: getKnowledgeDocument</div>
                      </div>
                      <div class="code-box">{
  "name": "getKnowledgeDocument",
  "description": "Reads raw content of a document from knowledge base",
  "inputSchema": {
    "type": "object",
    "properties": {
      "path": { "type": "string", "description": "Relative path (e.g. java/collections.md)" }
    },
    "required": ["path"]
  }
}</div>
                    </div>

                    <div class="card">
                      <div class="card-header">
                        <div class="card-title">Interactive Tool Invocation Test</div>
                      </div>
                      <div class="form-group">
                        <label for="mcp-input-path">Target Path</label>
                        <input type="text" id="mcp-input-path" class="code-input" value="java/collections.md">
                      </div>
                      <div style="display: flex; gap: 12px;">
                        <button class="btn btn-primary" id="mcp-submit-btn" onclick="executeMcpTool('java/collections.md')">
                          Invoke Tool
                        </button>
                        <button class="btn btn-secondary" onclick="executeMcpTool('../pom.xml')">
                          Test Traversal Rejection (../pom.xml)
                        </button>
                      </div>
                    </div>

                    <div class="banner-error" id="mcp-error-banner"></div>

                    <div id="mcp-result-stage" class="card" style="display: none;">
                      <div class="card-header">
                        <div class="card-title">Invocation Result Output</div>
                        <span class="badge badge-success" id="mcp-outcome-badge">SUCCESS</span>
                      </div>
                      <div class="code-box" id="mcp-output-json">Awaiting response...</div>
                    </div>
                  </div>
                </main>
              </div>

              <script>
                function switchTab(tabId) {
                  document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
                  document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

                  const activeBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.getAttribute('onclick').includes(tabId));
                  if (activeBtn) activeBtn.classList.add('active');

                  const activePane = document.getElementById('tab-' + tabId);
                  if (activePane) activePane.classList.add('active');

                  const titles = {
                    'knowledge': 'Knowledge Base Inspector',
                    'rag': 'RAG Observability Console',
                    'agent': 'Knowledge Agent Execution Trace',
                    'mcp': 'MCP Protocol Inspector'
                  };
                  document.getElementById('current-view-title').textContent = titles[tabId] || 'Developer Console';
                }

                async function loadKnowledgeDocs() {
                  try {
                    const res = await fetch('api/knowledge/documents');
                    const docs = await res.json();
                    const ul = document.getElementById('doc-list-ul');
                    ul.innerHTML = '';
                    docs.forEach(doc => {
                      const li = document.createElement('li');
                      li.className = 'doc-item';
                      li.onclick = () => viewDocDetails(doc.sourcePath);
                      li.innerHTML = `<span class="doc-path">${escapeHtml(doc.sourcePath)}</span><span class="badge badge-neutral">${escapeHtml(doc.id)}</span>`;
                      ul.appendChild(li);
                    });
                    document.getElementById('stat-doc-count').textContent = docs.length;
                  } catch (e) {
                    console.error('Failed to load documents', e);
                  }
                }

                async function viewDocDetails(path) {
                  const card = document.getElementById('doc-detail-card');
                  const title = document.getElementById('doc-detail-title');
                  const content = document.getElementById('doc-detail-content');
                  card.style.display = 'block';
                  title.textContent = 'Document: ' + path;
                  content.textContent = 'Loading document content...';

                  try {
                    const res = await fetch('api/knowledge/document?path=' + encodeURIComponent(path));
                    const doc = await res.json();
                    content.textContent = doc.content || JSON.stringify(doc, null, 2);
                  } catch (e) {
                    content.textContent = 'Error loading document details: ' + e.message;
                  }
                }

                async function executeRagPipeline() {
                  const query = document.getElementById('rag-input-query').value.trim();
                  if (!query) return;

                  const btn = document.getElementById('rag-submit-btn');
                  const stage = document.getElementById('rag-result-stage');
                  const errBanner = document.getElementById('rag-error-banner');
                  const answerEl = document.getElementById('rag-answer-output');
                  const retrievalEl = document.getElementById('rag-retrieval-container');

                  btn.disabled = true;
                  btn.textContent = 'Executing Pipeline...';
                  errBanner.style.display = 'none';
                  stage.style.display = 'block';
                  answerEl.textContent = 'Retrieving context and invoking gemini-3.6-flash...';
                  retrievalEl.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">Processing retrieval search...</div>';

                  try {
                    const res = await fetch('api/rag/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    answerEl.textContent = data.generatedAnswer;

                    if (data.retrievedChunks && data.retrievedChunks.length > 0) {
                      retrievalEl.innerHTML = '';
                      data.retrievedChunks.forEach(chunk => {
                        const scorePct = (chunk.relevanceScore * 100).toFixed(1);
                        const card = document.createElement('div');
                        card.style.cssText = 'background:var(--surface-subtle); border:1px solid var(--border-color); padding:12px; border-radius:6px; margin-bottom:10px;';
                        card.innerHTML = `
                          <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-bottom:6px;">
                            <span class="doc-path">${escapeHtml(chunk.sourcePath)} (Chunk #${chunk.chunkIndex})</span>
                            <span class="badge badge-success">Similarity Score: ${chunk.relevanceScore.toFixed(4)} (${scorePct}%)</span>
                          </div>
                          <div class="code-box" style="margin-top:6px; max-height:120px;">${escapeHtml(chunk.chunkText)}</div>
                        `;
                        retrievalEl.appendChild(card);
                      });
                    } else {
                      retrievalEl.innerHTML = '<div style="font-size:0.85rem; color:var(--text-muted); padding:12px; background:var(--surface-subtle); border-radius:6px;">No chunks matched similarity threshold (>= 0.70).</div>';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'RAG Failure: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Execute RAG Pipeline';
                  }
                }

                async function executeAgentLoop() {
                  const query = document.getElementById('agent-input-query').value.trim();
                  if (!query) return;

                  const btn = document.getElementById('agent-submit-btn');
                  const stage = document.getElementById('agent-result-stage');
                  const errBanner = document.getElementById('agent-error-banner');
                  const timeline = document.getElementById('agent-trace-timeline');
                  const finalAns = document.getElementById('agent-final-answer');

                  btn.disabled = true;
                  btn.textContent = 'Evaluating Agent Loop...';
                  errBanner.style.display = 'none';
                  stage.style.display = 'block';
                  timeline.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">Executing decision step 1...</div>';
                  finalAns.textContent = 'Awaiting loop completion...';

                  try {
                    const res = await fetch('api/agent/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    finalAns.textContent = data.answer;
                    timeline.innerHTML = '';

                    if (data.trace && data.trace.length > 0) {
                      data.trace.forEach(t => {
                        const stepDiv = document.createElement('div');
                        stepDiv.className = 'trace-step';
                        const badge = t.toolResultSuccess ? '<span class="badge badge-success">SUCCESS</span>' : '<span class="badge badge-error">RESULT</span>';
                        stepDiv.innerHTML = `
                          <div class="trace-step-header">
                            <span class="trace-step-type">Step ${t.step}: ${escapeHtml(t.decisionType)}</span>
                            <div>Tool: <code style="font-family:var(--mono-font);">${escapeHtml(t.toolName)}</code> &nbsp; ${badge}</div>
                          </div>
                          <div class="code-box" style="margin-top:6px;">${escapeHtml(t.toolOutput)}</div>
                        `;
                        timeline.appendChild(stepDiv);
                      });
                    } else {
                      timeline.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">No trace steps recorded.</div>';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'Agent Loop Failure: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Run Agent Decision Loop';
                  }
                }

                async function executeMcpTool(pathOverride) {
                  const inputPath = pathOverride || document.getElementById('mcp-input-path').value.trim();
                  document.getElementById('mcp-input-path').value = inputPath;

                  const btn = document.getElementById('mcp-submit-btn');
                  const stage = document.getElementById('mcp-result-stage');
                  const errBanner = document.getElementById('mcp-error-banner');
                  const badge = document.getElementById('mcp-outcome-badge');
                  const outputJson = document.getElementById('mcp-output-json');

                  btn.disabled = true;
                  btn.textContent = 'Invoking MCP Tool...';
                  errBanner.style.display = 'none';
                  stage.style.display = 'block';
                  outputJson.textContent = 'Communicating with McpKnowledgeServer over STDIO...';

                  try {
                    const res = await fetch('api/mcp/test', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({path: inputPath})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    outputJson.textContent = JSON.stringify(data.output || data, null, 2);

                    if (data.success) {
                      badge.className = 'badge badge-success';
                      badge.textContent = 'SUCCESS';
                    } else {
                      badge.className = 'badge badge-error';
                      badge.textContent = 'REJECTED (BOUNDARY BLOCKED)';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'MCP Invocation Error: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Invoke Tool';
                  }
                }

                function escapeHtml(str) {
                  if (!str) return '';
                  return String(str)
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#039;');
                }
              </script>
            </body>
            </html>
            """;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(CONSOLE_HTML);
    }
}
