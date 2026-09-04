package com.shevay.knowledge.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet serving the unified Developer Console at GET / and GET /console.
 * Designed with a high-density, engineering workstation black/white/neon aesthetic.
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
                  --bg-app: #09090b;
                  --sidebar-bg: #09090b;
                  --sidebar-border: rgba(255, 255, 255, 0.08);
                  --sidebar-text: #71717a;
                  --sidebar-text-active: #ffffff;
                  --sidebar-active-bg: #18181b;
                  --surface-card: #121212;
                  --surface-subtle: #18181b;
                  --border-color: rgba(255, 255, 255, 0.08);
                  --border-dark: rgba(255, 255, 255, 0.16);
                  --text-primary: #ffffff;
                  --text-muted: #a1a1aa;
                  --text-code: #f4f4f5;
                  --accent: #ffffff;
                  --accent-glow: 0 0 12px rgba(255, 255, 255, 0.15);
                  --success: #22c55e;
                  --success-bg: rgba(34, 197, 94, 0.1);
                  --error: #ef4444;
                  --error-bg: rgba(239, 68, 68, 0.1);
                  --warning: #f59e0b;
                  --warning-bg: rgba(245, 158, 11, 0.1);
                  --mono-font: "JetBrains Mono", "IBM Plex Mono", SFMono-Regular, Consolas, monospace;
                  --sans-font: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                  --radius: 8px;
                  --radius-sm: 6px;
                }

                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  font-family: var(--sans-font);
                  background-color: var(--bg-app);
                  color: var(--text-primary);
                  line-height: 1.5;
                  font-size: 0.875rem;
                  -webkit-font-smoothing: antialiased;
                }

                /* App Layout */
                .app-layout {
                  display: flex;
                  min-height: 100vh;
                }

                /* Sidebar */
                .sidebar {
                  width: 250px;
                  background-color: var(--sidebar-bg);
                  border-right: 1px solid var(--sidebar-border);
                  color: var(--sidebar-text);
                  padding: 24px 16px;
                  display: flex;
                  flex-direction: column;
                  flex-shrink: 0;
                  justify-content: space-between;
                }

                .brand-header {
                  padding-bottom: 20px;
                  margin-bottom: 24px;
                  border-bottom: 1px solid var(--sidebar-border);
                }

                .brand-title {
                  color: #ffffff;
                  font-weight: 700;
                  font-size: 1rem;
                  letter-spacing: -0.01em;
                }

                .brand-subtitle {
                  font-size: 0.75rem;
                  color: #71717a;
                  margin-top: 2px;
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
                  color: var(--sidebar-text);
                  padding: 9px 12px;
                  border-radius: var(--radius-sm);
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
                  color: #ffffff;
                }

                .tab-btn.active {
                  background-color: var(--sidebar-active-bg);
                  color: var(--sidebar-text-active);
                  border-color: rgba(255, 255, 255, 0.25);
                  box-shadow: 0 0 8px rgba(255, 255, 255, 0.08);
                  font-weight: 600;
                }

                .sidebar-footer {
                  padding-top: 16px;
                  border-top: 1px solid var(--sidebar-border);
                  font-size: 0.75rem;
                  color: #71717a;
                }

                .sidebar-footer-title {
                  color: #a1a1aa;
                  font-weight: 600;
                }

                /* Main Content Stage */
                .main-stage {
                  flex: 1;
                  padding: 32px;
                  overflow-y: auto;
                  max-width: 1240px;
                }

                /* Top Header */
                .top-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: flex-start;
                  margin-bottom: 24px;
                }

                .header-title-section h1 {
                  font-size: 1.4rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  letter-spacing: -0.02em;
                }

                .header-title-section p {
                  font-size: 0.85rem;
                  color: var(--text-muted);
                  margin-top: 2px;
                }

                .status-badge-ready {
                  display: inline-flex;
                  align-items: center;
                  gap: 6px;
                  font-size: 0.75rem;
                  font-weight: 600;
                  color: var(--success);
                  background-color: var(--success-bg);
                  border: 1px solid rgba(34, 197, 94, 0.2);
                  padding: 4px 10px;
                  border-radius: 20px;
                }

                .dot-green {
                  width: 6px;
                  height: 6px;
                  border-radius: 50%;
                  background-color: var(--success);
                }

                /* Metric Strip */
                .metric-strip {
                  display: grid;
                  grid-template-columns: repeat(5, 1fr);
                  gap: 12px;
                  margin-bottom: 24px;
                }

                .metric-box {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 14px;
                }

                .metric-lbl {
                  font-size: 0.725rem;
                  color: var(--text-muted);
                  font-weight: 500;
                  margin-bottom: 4px;
                }

                .metric-val-text {
                  font-size: 0.95rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  font-family: var(--mono-font);
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }

                /* Card Surface */
                .card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 24px;
                  margin-bottom: 24px;
                }

                .card-title-row {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 16px;
                }

                .card-h2 {
                  font-size: 1rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  letter-spacing: -0.01em;
                }

                .card-desc {
                  font-size: 0.825rem;
                  color: var(--text-muted);
                  margin-top: 2px;
                }

                /* Inputs & Buttons */
                .search-input-group {
                  display: flex;
                  gap: 12px;
                  margin-top: 16px;
                  margin-bottom: 16px;
                }

                .text-input {
                  flex: 1;
                  padding: 11px 14px;
                  font-size: 0.875rem;
                  font-family: var(--sans-font);
                  background-color: var(--surface-subtle);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  color: var(--text-primary);
                  transition: all 0.15s ease;
                }

                .text-input:focus {
                  outline: none;
                  border-color: var(--border-dark);
                  box-shadow: 0 0 8px rgba(255, 255, 255, 0.1);
                }

                .btn-primary {
                  background-color: #ffffff;
                  color: #000000;
                  border: none;
                  padding: 11px 20px;
                  font-size: 0.85rem;
                  font-weight: 600;
                  border-radius: var(--radius-sm);
                  cursor: pointer;
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  gap: 8px;
                  transition: all 0.15s ease;
                  white-space: nowrap;
                }

                .btn-primary:hover {
                  background-color: #f4f4f5;
                  box-shadow: 0 0 12px rgba(255, 255, 255, 0.2);
                }

                .btn-primary:disabled {
                  opacity: 0.5;
                  cursor: not-allowed;
                }

                .btn-secondary {
                  background-color: var(--surface-subtle);
                  color: var(--text-primary);
                  border: 1px solid var(--border-color);
                  padding: 8px 14px;
                  font-size: 0.825rem;
                  font-weight: 600;
                  border-radius: var(--radius-sm);
                  cursor: pointer;
                  display: inline-flex;
                  align-items: center;
                  gap: 6px;
                  transition: all 0.15s ease;
                }

                .btn-secondary:hover {
                  background-color: #27272a;
                  border-color: var(--border-dark);
                }

                /* Secondary Suggestions */
                .suggestions-row {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  flex-wrap: wrap;
                  margin-top: 12px;
                }

                .suggestions-lbl {
                  font-size: 0.75rem;
                  color: var(--text-muted);
                  font-weight: 500;
                  margin-right: 4px;
                }

                .chip-btn {
                  background-color: var(--surface-subtle);
                  border: 1px solid var(--border-color);
                  color: var(--text-muted);
                  padding: 4px 10px;
                  border-radius: 4px;
                  font-size: 0.775rem;
                  font-weight: 500;
                  cursor: pointer;
                  transition: all 0.15s ease;
                }

                .chip-btn:hover {
                  background-color: #27272a;
                  color: #ffffff;
                  border-color: var(--border-dark);
                }

                /* Minimal Empty State */
                .empty-state-card {
                  padding: 40px 24px;
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  margin-bottom: 24px;
                  text-align: left;
                }

                .empty-state-card h3 {
                  font-size: 1rem;
                  font-weight: 600;
                  color: var(--text-primary);
                  margin-bottom: 4px;
                }

                .empty-state-card p {
                  font-size: 0.85rem;
                  color: var(--text-muted);
                  line-height: 1.5;
                }

                /* Results Display */
                .result-block {
                  margin-bottom: 24px;
                }

                .result-section-label {
                  font-size: 0.725rem;
                  font-weight: 700;
                  letter-spacing: 0.08em;
                  color: var(--text-muted);
                  text-transform: uppercase;
                  margin-bottom: 8px;
                }

                .answer-box {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 20px;
                  font-size: 0.9rem;
                  color: var(--text-primary);
                  line-height: 1.6;
                }

                .source-row-card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 16px;
                  margin-bottom: 12px;
                }

                .source-row-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 8px;
                }

                .source-path {
                  font-family: var(--mono-font);
                  font-size: 0.85rem;
                  font-weight: 600;
                  color: #ffffff;
                }

                .sim-badge {
                  font-family: var(--mono-font);
                  font-size: 0.725rem;
                  font-weight: 600;
                  background-color: var(--success-bg);
                  color: var(--success);
                  border: 1px solid rgba(34, 197, 94, 0.2);
                  padding: 2px 8px;
                  border-radius: 4px;
                }

                .chunk-snippet {
                  background-color: var(--surface-subtle);
                  border: 1px solid var(--border-color);
                  border-radius: 4px;
                  padding: 12px;
                  font-family: var(--mono-font);
                  font-size: 0.8rem;
                  color: var(--text-code);
                  white-space: pre-wrap;
                  word-break: break-word;
                }

                /* Expandable Execution Details */
                details.exec-details {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 12px 16px;
                  font-size: 0.825rem;
                  margin-top: 16px;
                }

                details.exec-details summary {
                  font-weight: 600;
                  color: var(--text-muted);
                  cursor: pointer;
                  user-select: none;
                }

                details.exec-details summary:hover {
                  color: var(--text-primary);
                }

                .exec-table {
                  width: 100%;
                  margin-top: 12px;
                  border-collapse: collapse;
                  font-size: 0.8rem;
                }

                .exec-table td {
                  padding: 6px 12px 6px 0;
                  border-bottom: 1px dashed var(--border-color);
                }

                .exec-table td.lbl {
                  color: var(--text-muted);
                  width: 180px;
                }

                .exec-table td.val {
                  font-family: var(--mono-font);
                  color: var(--text-primary);
                  font-weight: 500;
                }

                /* Technical Data Table */
                .doc-table {
                  width: 100%;
                  border-collapse: collapse;
                  margin-top: 8px;
                }

                .doc-table th {
                  text-align: left;
                  font-size: 0.725rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.05em;
                  color: var(--text-muted);
                  padding: 10px 16px;
                  border-bottom: 1px solid var(--border-color);
                }

                .doc-table td {
                  padding: 12px 16px;
                  border-bottom: 1px solid var(--border-color);
                  font-size: 0.85rem;
                }

                .doc-table tr:hover td {
                  background-color: var(--surface-subtle);
                  cursor: pointer;
                }

                /* Agent Execution Timeline */
                .trace-timeline {
                  display: flex;
                  flex-direction: column;
                  gap: 14px;
                  margin-top: 16px;
                  position: relative;
                  padding-left: 20px;
                  border-left: 2px solid var(--border-color);
                }

                .trace-node {
                  position: relative;
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 14px 16px;
                }

                .trace-node::before {
                  content: '';
                  position: absolute;
                  left: -27px;
                  top: 18px;
                  width: 10px;
                  height: 10px;
                  border-radius: 50%;
                  background-color: #ffffff;
                  box-shadow: 0 0 6px rgba(255, 255, 255, 0.4);
                }

                .trace-node-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 6px;
                }

                .trace-node-title {
                  font-weight: 700;
                  font-size: 0.85rem;
                  color: var(--text-primary);
                }

                /* Banner Error */
                .banner-error {
                  background-color: var(--error-bg);
                  border: 1px solid rgba(239, 68, 68, 0.2);
                  color: var(--error);
                  padding: 12px 16px;
                  border-radius: var(--radius-sm);
                  font-size: 0.85rem;
                  margin-bottom: 20px;
                  display: none;
                }

                /* Page Tab Panes */
                .tab-pane { display: none; }
                .tab-pane.active { display: block; }

                /* Footer Bottom */
                .page-footer {
                  margin-top: 48px;
                  padding-top: 20px;
                  border-top: 1px solid var(--border-color);
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  font-size: 0.75rem;
                  color: var(--text-muted);
                }

                /* Responsive */
                @media (max-width: 1024px) {
                  .metric-strip { grid-template-columns: repeat(3, 1fr); }
                }
                @media (max-width: 768px) {
                  .app-layout { flex-direction: column; }
                  .sidebar { width: 100%; border-right: none; border-bottom: 1px solid var(--sidebar-border); }
                  .main-stage { padding: 20px; }
                  .metric-strip { grid-template-columns: repeat(2, 1fr); }
                  .top-header { flex-direction: column; gap: 12px; }
                  .search-input-group { flex-direction: column; }
                }
              </style>
            </head>
            <body>
              <div class="app-layout">
                <!-- Sidebar -->
                <aside class="sidebar">
                  <div>
                    <div class="brand-header">
                      <div class="brand-title">Maven Knowledge Lab</div>
                      <div class="brand-subtitle">Developer Knowledge Assistant</div>
                    </div>

                    <ul class="nav-menu">
                      <li>
                        <button class="tab-btn active" id="nav-rag" onclick="switchView('rag')">
                          RAG Pipeline
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-knowledge" onclick="switchView('knowledge')">
                          Knowledge Base
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-agent" onclick="switchView('agent')">
                          Knowledge Agent
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-mcp" onclick="switchView('mcp')">
                          MCP Protocol
                        </button>
                      </li>
                    </ul>
                  </div>

                  <div class="sidebar-footer">
                    <div class="sidebar-footer-title">Maven Knowledge Lab</div>
                    <div>v1.0.0</div>
                  </div>
                </aside>

                <!-- Main Content Stage -->
                <main class="main-stage">
                  <!-- Top Bar Header -->
                  <div class="top-header">
                    <div class="header-title-section">
                      <h1 id="page-title">RAG Pipeline</h1>
                      <p id="page-subtitle">Search the Maven knowledge base and get a grounded answer.</p>
                    </div>
                    <div class="status-badge-ready">
                      <span class="dot-green"></span>
                      System Ready
                    </div>
                  </div>

                  <!-- Metric Telemetry Strip -->
                  <div class="metric-strip">
                    <div class="metric-box">
                      <div class="metric-lbl">Runtime</div>
                      <div class="metric-val-text" style="color:var(--success);">Online</div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-lbl">LLM Model</div>
                      <div class="metric-val-text">gemini-3.6-flash</div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-lbl">Vector Dimension</div>
                      <div class="metric-val-text">768</div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-lbl">Knowledge Documents</div>
                      <div class="metric-val-text" id="stat-docs">4</div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-lbl">Vector Records</div>
                      <div class="metric-val-text" id="stat-vectors">5</div>
                    </div>
                  </div>

                  <!-- VIEW 1: RAG PIPELINE VIEW -->
                  <div id="pane-rag" class="tab-pane active">
                    <div class="card">
                      <div class="card-h2">Ask a Question</div>
                      <div class="card-desc">Search the Maven knowledge base and get a grounded answer.</div>

                      <div class="search-input-group">
                        <input type="text" id="rag-query-input" class="text-input" placeholder="What would you like to know about Maven?" value="What is the Maven lifecycle?">
                        <button class="btn-primary" id="rag-run-btn" onclick="executeRagSearch()">
                          Run Search
                        </button>
                      </div>

                      <div class="suggestions-row">
                        <span class="suggestions-lbl">Try an example:</span>
                        <button class="chip-btn" onclick="applySuggestion('What is the Maven lifecycle?')">What is the Maven lifecycle?</button>
                        <button class="chip-btn" onclick="applySuggestion('Explain Maven dependency scopes')">Explain Maven dependency scopes</button>
                        <button class="chip-btn" onclick="applySuggestion('How does Maven handle plugins?')">How does Maven handle plugins?</button>
                        <button class="chip-btn" onclick="applySuggestion('What is dependency management?')">What is dependency management?</button>
                      </div>
                    </div>

                    <div class="banner-error" id="rag-error-box"></div>

                    <!-- RAG Empty State (Text-first, minimal) -->
                    <div id="rag-empty-state" class="empty-state-card">
                      <h3>Ready to search</h3>
                      <p>Ask a question above to retrieve relevant documentation and generate a grounded answer.</p>
                    </div>

                    <!-- RAG Output Stage -->
                    <div id="rag-result-stage" style="display: none;">
                      <div class="result-block">
                        <div class="result-section-label">Answer</div>
                        <div class="answer-box" id="rag-answer-body">Generating grounded answer...</div>
                      </div>

                      <div class="result-block">
                        <div class="result-section-label">Sources</div>
                        <div id="rag-sources-container"></div>
                      </div>

                      <details class="exec-details">
                        <summary>Execution details</summary>
                        <table class="exec-table">
                          <tr><td class="lbl">Embedding Provider</td><td class="val">Gemini</td></tr>
                          <tr><td class="lbl">Embedding Model</td><td class="val">gemini-embedding-001</td></tr>
                          <tr><td class="lbl">Retrieved Chunks</td><td class="val" id="detail-chunk-count">1</td></tr>
                          <tr><td class="lbl">Similarity Threshold</td><td class="val">0.70</td></tr>
                          <tr><td class="lbl">Generation Model</td><td class="val">gemini-3.6-flash</td></tr>
                        </table>
                      </details>
                    </div>
                  </div>

                  <!-- VIEW 2: KNOWLEDGE BASE VIEW -->
                  <div id="pane-knowledge" class="tab-pane">
                    <div class="card">
                      <div class="card-title-row">
                        <div>
                          <div class="card-h2">Documents</div>
                          <div class="card-desc">Indexed technical documentation available to the assistant.</div>
                        </div>
                        <button class="btn-secondary" onclick="fetchDocs()">
                          Refresh
                        </button>
                      </div>

                      <table class="doc-table">
                        <thead>
                          <tr>
                            <th>Source Document Path</th>
                            <th>Document Key</th>
                          </tr>
                        </thead>
                        <tbody id="doc-table-body">
                          <tr onclick="inspectDocument('java/collections.md')">
                            <td class="source-path">java/collections.md</td>
                            <td style="font-family:var(--mono-font); color:var(--text-muted);">doc-java-collections</td>
                          </tr>
                          <tr onclick="inspectDocument('java/concurrency.md')">
                            <td class="source-path">java/concurrency.md</td>
                            <td style="font-family:var(--mono-font); color:var(--text-muted);">doc-java-concurrency</td>
                          </tr>
                          <tr onclick="inspectDocument('maven/dependencies.md')">
                            <td class="source-path">maven/dependencies.md</td>
                            <td style="font-family:var(--mono-font); color:var(--text-muted);">doc-maven-dependencies</td>
                          </tr>
                          <tr onclick="inspectDocument('maven/lifecycle.md')">
                            <td class="source-path">maven/lifecycle.md</td>
                            <td style="font-family:var(--mono-font); color:var(--text-muted);">doc-maven-lifecycle</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>

                    <div class="card" id="doc-preview-card" style="display: none;">
                      <div class="card-title-row">
                        <div class="card-h2" id="doc-preview-title">Document Content</div>
                      </div>
                      <div class="chunk-snippet" id="doc-preview-body" style="max-height: 400px; overflow-y: auto;">Select a document to inspect payload.</div>
                    </div>
                  </div>

                  <!-- VIEW 3: KNOWLEDGE AGENT VIEW -->
                  <div id="pane-agent" class="tab-pane">
                    <div class="card">
                      <div class="card-h2">Knowledge Agent</div>
                      <div class="card-desc">Investigate Maven knowledge through controlled tool calls.</div>

                      <div class="search-input-group">
                        <input type="text" id="agent-query-input" class="text-input" placeholder="What should the agent investigate?" value="Explain Maven dependency scopes">
                        <button class="btn-primary" id="agent-run-btn" onclick="executeAgentLoop()">
                          Run Agent
                        </button>
                      </div>
                    </div>

                    <div class="banner-error" id="agent-error-box"></div>

                    <div id="agent-result-stage" style="display: none;">
                      <div class="result-block">
                        <div class="result-section-label">Agent Trace</div>
                        <div class="trace-timeline" id="agent-trace-list"></div>
                      </div>

                      <div class="result-block">
                        <div class="result-section-label">Final Answer</div>
                        <div class="answer-box" id="agent-answer-body">Awaiting decision loop...</div>
                      </div>
                    </div>
                  </div>

                  <!-- VIEW 4: MCP PROTOCOL VIEW -->
                  <div id="pane-mcp" class="tab-pane">
                    <div class="card">
                      <div class="card-title-row">
                        <div>
                          <div class="card-h2">MCP Protocol</div>
                          <div class="card-desc">Inspect the MCP client and server interaction.</div>
                        </div>
                        <span class="sim-badge" style="font-size:0.8rem;">CONNECTED</span>
                      </div>

                      <div style="display: flex; gap: 10px; align-items: center; background:var(--surface-subtle); padding:10px 14px; border-radius:var(--radius-sm); border:1px solid var(--border-color); font-family:var(--mono-font); font-size:0.8rem; margin-bottom: 20px;">
                        <span>Browser</span> &rarr;
                        <span style="color:#ffffff; font-weight:600;">MCP Client</span> &rarr;
                        <span style="color:#a1a1aa; font-weight:600;">STDIO</span> &rarr;
                        <span style="color:#a1a1aa; font-weight:600;">MCP Server</span> &rarr;
                        <span style="color:var(--success); font-weight:600;">Tool</span>
                      </div>

                      <details class="exec-details" open>
                        <summary style="font-size:0.85rem;">Tool: getKnowledgeDocument</summary>
                        <div class="chunk-snippet" style="margin-top:10px;">{
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
                      </details>
                    </div>

                    <div class="card">
                      <div class="card-h2" style="font-size:0.95rem; margin-bottom:12px;">Tool Invocation</div>
                      <div class="search-input-group" style="margin-top:0;">
                        <input type="text" id="mcp-path-input" class="text-input" style="font-family:var(--mono-font);" value="java/collections.md">
                        <button class="btn-primary" id="mcp-run-btn" onclick="invokeMcpTool('java/collections.md')">
                          Invoke Tool
                        </button>
                        <button class="btn-secondary" onclick="invokeMcpTool('../pom.xml')">
                          Test Traversal Rejection (../pom.xml)
                        </button>
                      </div>
                    </div>

                    <div class="banner-error" id="mcp-error-box"></div>

                    <div id="mcp-result-stage" class="card" style="display: none;">
                      <div class="card-title-row">
                        <div class="card-h2" style="font-size:0.9rem;">Result Output</div>
                        <span class="sim-badge" id="mcp-status-badge">SUCCESS</span>
                      </div>
                      <div class="chunk-snippet" id="mcp-output-body">Awaiting response...</div>
                    </div>
                  </div>

                  <!-- Footer -->
                  <footer class="page-footer">
                    <div>Maven Knowledge Lab &middot; Developer Knowledge Assistant</div>
                    <div>v1.0.0</div>
                  </footer>
                </main>
              </div>

              <script>
                function switchView(viewId) {
                  document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
                  document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));

                  const navBtn = document.getElementById('nav-' + viewId);
                  if (navBtn) navBtn.classList.add('active');

                  const pane = document.getElementById('pane-' + viewId);
                  if (pane) pane.classList.add('active');

                  const meta = {
                    'rag': { title: 'RAG Pipeline', sub: 'Search the Maven knowledge base and get a grounded answer.' },
                    'knowledge': { title: 'Knowledge Base', sub: 'Indexed technical documentation available to the assistant.' },
                    'agent': { title: 'Knowledge Agent', sub: 'Investigate Maven knowledge through controlled tool calls.' },
                    'mcp': { title: 'MCP Protocol', sub: 'Inspect the MCP client and server interaction.' }
                  };

                  const currentMeta = meta[viewId] || meta['rag'];
                  document.getElementById('page-title').textContent = currentMeta.title;
                  document.getElementById('page-subtitle').textContent = currentMeta.sub;
                }

                function applySuggestion(queryText) {
                  const input = document.getElementById('rag-query-input');
                  input.value = queryText;
                  executeRagSearch();
                }

                async function fetchDocs() {
                  try {
                    const res = await fetch('api/knowledge/documents');
                    const docs = await res.json();
                    const tbody = document.getElementById('doc-table-body');
                    tbody.innerHTML = '';
                    docs.forEach(doc => {
                      const tr = document.createElement('tr');
                      tr.onclick = () => inspectDocument(doc.sourcePath);
                      tr.innerHTML = `<td class="source-path">${escapeHtml(doc.sourcePath)}</td><td style="font-family:var(--mono-font); color:var(--text-muted);">${escapeHtml(doc.id)}</td>`;
                      tbody.appendChild(tr);
                    });
                    document.getElementById('stat-docs').textContent = docs.length;
                  } catch (e) {
                    console.error('Failed to load docs', e);
                  }
                }

                async function inspectDocument(path) {
                  const card = document.getElementById('doc-preview-card');
                  const title = document.getElementById('doc-preview-title');
                  const body = document.getElementById('doc-preview-body');
                  card.style.display = 'block';
                  title.textContent = 'Document: ' + path;
                  body.textContent = 'Loading payload...';

                  try {
                    const res = await fetch('api/knowledge/document?path=' + encodeURIComponent(path));
                    const data = await res.json();
                    body.textContent = data.content || JSON.stringify(data, null, 2);
                  } catch (e) {
                    body.textContent = 'Error loading document details: ' + e.message;
                  }
                }

                async function executeRagSearch() {
                  const query = document.getElementById('rag-query-input').value.trim();
                  if (!query) return;

                  const btn = document.getElementById('rag-run-btn');
                  const emptyState = document.getElementById('rag-empty-state');
                  const stage = document.getElementById('rag-result-stage');
                  const errBox = document.getElementById('rag-error-box');
                  const answerBody = document.getElementById('rag-answer-body');
                  const sourcesContainer = document.getElementById('rag-sources-container');
                  const chunkCountVal = document.getElementById('detail-chunk-count');

                  btn.disabled = true;
                  btn.textContent = 'Searching...';
                  errBox.style.display = 'none';
                  emptyState.style.display = 'none';
                  stage.style.display = 'block';
                  answerBody.textContent = 'Searching vector store and generating grounded answer...';
                  sourcesContainer.innerHTML = '';

                  try {
                    const res = await fetch('api/rag/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    answerBody.textContent = data.generatedAnswer;

                    if (data.retrievedChunks && data.retrievedChunks.length > 0) {
                      chunkCountVal.textContent = data.retrievedChunks.length;
                      data.retrievedChunks.forEach(chunk => {
                        const card = document.createElement('div');
                        card.className = 'source-row-card';
                        card.innerHTML = `
                          <div class="source-row-header">
                            <span class="source-path">${escapeHtml(chunk.sourcePath)}</span>
                            <span class="sim-badge">Similarity ${chunk.relevanceScore.toFixed(3)}</span>
                          </div>
                          <div class="chunk-snippet">${escapeHtml(chunk.chunkText)}</div>
                        `;
                        sourcesContainer.appendChild(card);
                      });
                    } else {
                      chunkCountVal.textContent = '0';
                      sourcesContainer.innerHTML = '<div style="font-size:0.85rem; color:var(--text-muted); padding:14px; background:var(--surface-card); border:1px solid var(--border-color); border-radius:var(--radius-sm);">No sources matched similarity threshold (&ge; 0.70).</div>';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    emptyState.style.display = 'block';
                    errBox.style.display = 'block';
                    errBox.textContent = 'Search error: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Run Search';
                  }
                }

                async function executeAgentLoop() {
                  const query = document.getElementById('agent-query-input').value.trim();
                  if (!query) return;

                  const btn = document.getElementById('agent-run-btn');
                  const stage = document.getElementById('agent-result-stage');
                  const errBox = document.getElementById('agent-error-box');
                  const traceList = document.getElementById('agent-trace-list');
                  const answerBody = document.getElementById('agent-answer-body');

                  btn.disabled = true;
                  btn.textContent = 'Running Agent...';
                  errBox.style.display = 'none';
                  stage.style.display = 'block';
                  traceList.innerHTML = '<div style="font-size:0.85rem; color:var(--text-muted);">Evaluating decision steps...</div>';
                  answerBody.textContent = 'Awaiting agent completion...';

                  try {
                    const res = await fetch('api/agent/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    answerBody.textContent = data.answer;
                    traceList.innerHTML = '';

                    // User query node
                    const userNode = document.createElement('div');
                    userNode.className = 'trace-node';
                    userNode.innerHTML = `<div class="trace-node-header"><span class="trace-node-title">USER QUERY</span></div><div style="font-size:0.85rem; color:var(--text-primary);">${escapeHtml(query)}</div>`;
                    traceList.appendChild(userNode);

                    if (data.trace && data.trace.length > 0) {
                      data.trace.forEach(t => {
                        const stepNode = document.createElement('div');
                        stepNode.className = 'trace-node';
                        const badge = t.toolResultSuccess
                          ? '<span class="sim-badge">Status: Success</span>'
                          : '<span class="sim-badge" style="background:var(--error-bg); color:var(--error); border-color:rgba(239,68,68,0.2);">Status: Output</span>';
                        stepNode.innerHTML = `
                          <div class="trace-node-header">
                            <span class="trace-node-title">STEP ${t.step} &middot; Tool Call</span>
                            <div><code style="font-family:var(--mono-font); font-weight:600; color:#ffffff;">${escapeHtml(t.toolName)}</code> &nbsp; ${badge}</div>
                          </div>
                          <details class="exec-details" style="margin-top:8px;">
                            <summary>View details</summary>
                            <div class="chunk-snippet" style="margin-top:8px;">${escapeHtml(t.toolOutput)}</div>
                          </details>
                        `;
                        traceList.appendChild(stepNode);
                      });
                    }

                    // Final answer node
                    const finalNode = document.createElement('div');
                    finalNode.className = 'trace-node';
                    finalNode.innerHTML = `<div class="trace-node-header"><span class="trace-node-title">STEP ${data.trace ? data.trace.length + 1 : 1} &middot; Final Answer</span></div><div style="font-size:0.85rem; color:var(--text-primary);">${escapeHtml(data.answer)}</div>`;
                    traceList.appendChild(finalNode);

                  } catch (e) {
                    stage.style.display = 'none';
                    errBox.style.display = 'block';
                    errBox.textContent = 'Agent execution error: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Run Agent';
                  }
                }

                async function invokeMcpTool(pathOverride) {
                  const inputPath = pathOverride || document.getElementById('mcp-path-input').value.trim();
                  document.getElementById('mcp-path-input').value = inputPath;

                  const btn = document.getElementById('mcp-run-btn');
                  const stage = document.getElementById('mcp-result-stage');
                  const errBox = document.getElementById('mcp-error-box');
                  const badge = document.getElementById('mcp-status-badge');
                  const outputBody = document.getElementById('mcp-output-body');

                  btn.disabled = true;
                  btn.textContent = 'Invoking...';
                  errBox.style.display = 'none';
                  stage.style.display = 'block';
                  outputBody.textContent = 'Communicating with McpKnowledgeServer over STDIO...';

                  try {
                    const res = await fetch('api/mcp/test', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({path: inputPath})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    outputBody.textContent = JSON.stringify(data.output || data, null, 2);

                    if (data.success) {
                      badge.className = 'sim-badge';
                      badge.textContent = 'SUCCESS';
                    } else {
                      badge.className = 'sim-badge';
                      badge.style.cssText = 'background:var(--error-bg); color:var(--error); border-color:rgba(239,68,68,0.2);';
                      badge.textContent = 'REJECTED (BOUNDARY BLOCKED)';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    errBox.style.display = 'block';
                    errBox.textContent = 'MCP error: ' + e.message;
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
