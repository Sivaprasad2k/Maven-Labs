package com.shevay.knowledge.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet serving the refined unified single-page Developer Console at GET / and GET /console.
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
                  --bg-color: #0b0f19;
                  --panel-bg: #151d2a;
                  --panel-header: #1c2638;
                  --border-color: #2a364f;
                  --text-main: #e2e8f0;
                  --text-muted: #94a3b8;
                  --accent: #3b82f6;
                  --accent-hover: #2563eb;
                  --code-bg: #090d16;
                  --success-bg: rgba(16, 185, 129, 0.15);
                  --success-text: #10b981;
                  --error-bg: rgba(239, 68, 68, 0.15);
                  --error-text: #f87171;
                  --mono-font: "JetBrains Mono", Consolas, Monaco, "Courier New", monospace;
                }

                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                  background-color: var(--bg-color);
                  color: var(--text-main);
                  line-height: 1.5;
                  padding: 20px;
                }

                .container {
                  max-width: 1140px;
                  margin: 0 auto;
                }

                header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  padding-bottom: 16px;
                  margin-bottom: 20px;
                  border-bottom: 1px solid var(--border-color);
                }

                .brand h1 {
                  font-size: 1.2rem;
                  font-weight: 700;
                  letter-spacing: -0.01em;
                  color: #ffffff;
                }

                .brand .subtitle {
                  font-size: 0.8rem;
                  color: var(--text-muted);
                  margin-top: 2px;
                }

                nav {
                  display: flex;
                  gap: 4px;
                  border-bottom: 1px solid var(--border-color);
                  margin-bottom: 20px;
                }

                .tab-btn {
                  background: none;
                  border: none;
                  color: var(--text-muted);
                  padding: 10px 18px;
                  font-size: 0.875rem;
                  font-weight: 600;
                  cursor: pointer;
                  border-bottom: 2px solid transparent;
                  transition: all 0.15s ease;
                }

                .tab-btn:hover {
                  color: var(--text-main);
                  background-color: rgba(255, 255, 255, 0.03);
                }

                .tab-btn:focus-visible {
                  outline: 2px solid var(--accent);
                  outline-offset: -2px;
                }

                .tab-btn.active {
                  color: var(--accent);
                  border-bottom-color: var(--accent);
                  background-color: rgba(59, 130, 246, 0.08);
                }

                .tab-content { display: none; }
                .tab-content.active { display: block; }

                .panel {
                  background-color: var(--panel-bg);
                  border: 1px solid var(--border-color);
                  border-radius: 4px;
                  padding: 20px;
                  margin-bottom: 20px;
                }

                .panel-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 14px;
                  padding-bottom: 10px;
                  border-bottom: 1px solid var(--border-color);
                }

                .panel-title {
                  font-size: 0.95rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.04em;
                  color: var(--text-main);
                }

                .form-group {
                  margin-bottom: 16px;
                }

                label {
                  display: block;
                  font-size: 0.8rem;
                  font-weight: 600;
                  text-transform: uppercase;
                  letter-spacing: 0.03em;
                  color: var(--text-muted);
                  margin-bottom: 6px;
                }

                input[type="text"] {
                  width: 100%;
                  background-color: var(--code-bg);
                  border: 1px solid var(--border-color);
                  border-radius: 4px;
                  color: var(--text-main);
                  padding: 10px 12px;
                  font-family: inherit;
                  font-size: 0.9rem;
                }

                input[type="text"]:focus {
                  outline: none;
                  border-color: var(--accent);
                }

                .btn {
                  background-color: var(--accent);
                  color: #ffffff;
                  border: none;
                  border-radius: 4px;
                  padding: 9px 16px;
                  font-size: 0.85rem;
                  font-weight: 600;
                  cursor: pointer;
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  transition: background-color 0.15s ease;
                }

                .btn:hover { background-color: var(--accent-hover); }
                .btn:disabled { opacity: 0.5; cursor: not-allowed; }

                .badge {
                  display: inline-block;
                  padding: 3px 8px;
                  border-radius: 3px;
                  font-size: 0.725rem;
                  font-weight: 700;
                  letter-spacing: 0.04em;
                  text-transform: uppercase;
                }

                .badge-success { background-color: var(--success-bg); color: var(--success-text); border: 1px solid rgba(16, 185, 129, 0.3); }
                .badge-error { background-color: var(--error-bg); color: var(--error-text); border: 1px solid rgba(239, 68, 68, 0.3); }

                pre, code {
                  font-family: var(--mono-font);
                  font-size: 0.825rem;
                }

                pre {
                  background-color: var(--code-bg);
                  border: 1px solid var(--border-color);
                  border-radius: 4px;
                  padding: 14px;
                  overflow-x: auto;
                  white-space: pre-wrap;
                  word-break: break-word;
                  line-height: 1.6;
                }

                .error-banner {
                  background-color: var(--error-bg);
                  border: 1px solid var(--error-text);
                  color: var(--error-text);
                  padding: 12px 14px;
                  border-radius: 4px;
                  font-size: 0.85rem;
                  margin-bottom: 16px;
                  display: none;
                }

                /* Knowledge Tab Grid Layout */
                .knowledge-grid {
                  display: grid;
                  grid-template-columns: 320px 1fr;
                  gap: 16px;
                }

                .doc-list {
                  list-style: none;
                  max-height: 480px;
                  overflow-y: auto;
                }

                .doc-item {
                  padding: 10px 12px;
                  border: 1px solid var(--border-color);
                  border-radius: 4px;
                  margin-bottom: 8px;
                  cursor: pointer;
                  background-color: var(--code-bg);
                  transition: all 0.15s ease;
                }

                .doc-item:hover {
                  border-color: var(--accent);
                }

                .doc-item.selected {
                  border-left: 3px solid var(--accent);
                  border-color: var(--accent);
                  background-color: rgba(59, 130, 246, 0.12);
                }

                .doc-item-title {
                  font-size: 0.875rem;
                  font-weight: 600;
                  color: var(--text-main);
                }

                .doc-item-path {
                  font-size: 0.75rem;
                  font-family: var(--mono-font);
                  color: var(--text-muted);
                  margin-top: 2px;
                }

                /* Agent Trace Timeline */
                .trace-timeline {
                  margin-top: 12px;
                }

                .trace-step {
                  display: flex;
                  gap: 12px;
                  margin-bottom: 14px;
                  padding: 12px;
                  background-color: var(--code-bg);
                  border: 1px solid var(--border-color);
                  border-left: 3px solid var(--accent);
                  border-radius: 4px;
                }

                .trace-step-num {
                  font-family: var(--mono-font);
                  font-size: 0.8rem;
                  font-weight: 700;
                  color: var(--accent);
                  min-width: 24px;
                }

                .trace-step-body {
                  flex-grow: 1;
                }

                .trace-step-header {
                  display: flex;
                  justify-content: space-between;
                  margin-bottom: 6px;
                  font-size: 0.8rem;
                }

                .diagram-box {
                  background-color: var(--code-bg);
                  border: 1px dashed var(--border-color);
                  border-radius: 4px;
                  padding: 12px 16px;
                  font-family: var(--mono-font);
                  font-size: 0.8rem;
                  color: var(--text-muted);
                  margin-bottom: 16px;
                  text-align: center;
                }

                @media (max-width: 768px) {
                  .knowledge-grid { grid-template-columns: 1fr; }
                }
              </style>
            </head>
            <body>

              <div class="container">
                <header>
                  <div class="brand">
                    <h1>Maven Knowledge Lab</h1>
                    <div class="subtitle">Unified Developer & Protocol Observation Console</div>
                  </div>
                  <div>
                    <span id="health-status" class="badge badge-success">API READY</span>
                  </div>
                </header>

                <nav role="tablist">
                  <button class="tab-btn active" role="tab" aria-selected="true" onclick="switchTab('knowledge')">Knowledge</button>
                  <button class="tab-btn" role="tab" aria-selected="false" onclick="switchTab('rag')">RAG</button>
                  <button class="tab-btn" role="tab" aria-selected="false" onclick="switchTab('agent')">Agent</button>
                  <button class="tab-btn" role="tab" aria-selected="false" onclick="switchTab('mcp')">MCP</button>
                </nav>

                <!-- 1. Knowledge Tab -->
                <div id="tab-knowledge" class="tab-content active" role="tabpanel">
                  <div class="knowledge-grid">
                    <div class="panel">
                      <div class="panel-header">
                        <span class="panel-title">Knowledge Corpus</span>
                        <button class="btn" onclick="loadDocuments()">Refresh List</button>
                      </div>
                      <div style="font-size: 0.75rem; color: var(--text-muted); margin-bottom: 10px;" id="doc-count-text">Click Refresh to scan documents</div>
                      <ul id="doc-list-container" class="doc-list">
                        <li class="doc-item-path" style="padding: 10px;">Corpus uninitialized. Click Refresh List.</li>
                      </ul>
                    </div>

                    <div class="panel">
                      <div class="panel-header">
                        <span class="panel-title">Document Viewer</span>
                        <span id="viewer-path-badge" class="doc-item-path">No selection</span>
                      </div>
                      <div id="doc-viewer-empty" style="padding: 30px; text-align: center; color: var(--text-muted); font-size: 0.85rem;">
                        Select a document from the corpus list on the left to inspect its raw text content.
                      </div>
                      <div id="doc-viewer-body" style="display: none;">
                        <h3 id="doc-viewer-title" style="font-size: 1rem; margin-bottom: 12px;"></h3>
                        <pre id="doc-viewer-content" style="max-height: 420px;"></pre>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 2. RAG Tab -->
                <div id="tab-rag" class="tab-content" role="tabpanel">
                  <div class="panel">
                    <div class="panel-header">
                      <span class="panel-title">Deterministic RAG Answer Generation</span>
                    </div>
                    <div class="form-group">
                      <label for="rag-input">Query</label>
                      <input type="text" id="rag-input" value="What is the Maven lifecycle?">
                    </div>
                    <button class="btn" id="rag-submit-btn" onclick="runRagQuery()">Ask RAG Pipeline</button>
                  </div>

                  <div id="rag-error-banner" class="error-banner"></div>

                  <div id="rag-result-panel" class="panel" style="display: none;">
                    <div class="panel-header">
                      <span class="panel-title">Generated Answer</span>
                    </div>
                    <pre id="rag-answer-output" style="margin-bottom: 20px;"></pre>

                    <div class="panel-header">
                      <span class="panel-title">Retrieved Sources</span>
                    </div>
                    <div id="rag-sources-container"></div>
                  </div>
                </div>

                <!-- 3. Agent Tab -->
                <div id="tab-agent" class="tab-content" role="tabpanel">
                  <div class="panel">
                    <div class="panel-header">
                      <span class="panel-title">Controlled Knowledge Agent</span>
                    </div>
                    <div class="form-group">
                      <label for="agent-input">Agent Task / Prompt</label>
                      <input type="text" id="agent-input" value="Explain Maven dependency scopes">
                    </div>
                    <button class="btn" id="agent-submit-btn" onclick="runAgentQuery()">Run Controlled Agent</button>
                  </div>

                  <div id="agent-error-banner" class="error-banner"></div>

                  <div id="agent-result-panel" class="panel" style="display: none;">
                    <div class="panel-header">
                      <span class="panel-title">Final Agent Answer</span>
                    </div>
                    <pre id="agent-answer-output" style="margin-bottom: 20px;"></pre>

                    <div class="panel-header">
                      <span class="panel-title">Execution Trace (Observe ➔ Decide ➔ Act)</span>
                    </div>
                    <div id="agent-trace-container" class="trace-timeline"></div>
                  </div>
                </div>

                <!-- 4. MCP Tab -->
                <div id="tab-mcp" class="tab-content" role="tabpanel">
                  <div class="diagram-box">
                    MCP CLIENT &nbsp;──(STDIO)──➔&nbsp; MCP SERVER &nbsp;──➔&nbsp; getKnowledgeDocument
                  </div>

                  <div class="panel">
                    <div class="panel-header">
                      <span class="panel-title">Model Context Protocol (MCP) Test</span>
                      <span class="badge badge-success">STDIO Transport</span>
                    </div>
                    <div class="form-group">
                      <label for="mcp-path-input">Document Path Argument</label>
                      <input type="text" id="mcp-path-input" value="java/collections.md">
                    </div>
                    <div style="display:flex; gap: 10px;">
                      <button class="btn" id="mcp-submit-btn" onclick="runMcpTest()">Invoke Tool</button>
                      <button class="btn" style="background-color: #475569;" onclick="testMcpSecurityTraversal()">Test Traversal Security (../pom.xml)</button>
                    </div>
                  </div>

                  <div id="mcp-error-banner" class="error-banner"></div>

                  <div id="mcp-result-panel" class="panel" style="display: none;">
                    <div class="panel-header">
                      <span class="panel-title">Discovered Tools</span>
                    </div>
                    <pre id="mcp-tools-output" style="margin-bottom: 20px;"></pre>

                    <div class="panel-header">
                      <span class="panel-title">Tool Invocation Result (getKnowledgeDocument)</span>
                      <span id="mcp-outcome-badge" class="badge badge-success">SUCCESS</span>
                    </div>
                    <pre id="mcp-invocation-output"></pre>
                  </div>
                </div>
              </div>

              <script>
                function switchTab(tabId) {
                  document.querySelectorAll('.tab-btn').forEach(btn => {
                    btn.classList.remove('active');
                    btn.setAttribute('aria-selected', 'false');
                  });
                  document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

                  const btn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.textContent.toLowerCase() === tabId);
                  if (btn) {
                    btn.classList.add('active');
                    btn.setAttribute('aria-selected', 'true');
                  }
                  const tab = document.getElementById('tab-' + tabId);
                  if (tab) tab.classList.add('active');
                }

                async function loadDocuments() {
                  const container = document.getElementById('doc-list-container');
                  const countText = document.getElementById('doc-count-text');
                  container.innerHTML = '<li class="doc-item-path" style="padding:10px;">Scanning corpus...</li>';
                  try {
                    const res = await fetch('api/knowledge/documents');
                    const data = await res.json();
                    if (!res.ok || data.error || !Array.isArray(data)) {
                      throw new Error(data.error || 'Failed to load documents');
                    }
                    container.innerHTML = '';
                    countText.textContent = data.length + ' documents loaded';
                    data.forEach(doc => {
                      const li = document.createElement('li');
                      li.className = 'doc-item';
                      li.innerHTML = `<div class="doc-item-title">${escapeHtml(doc.title)}</div><div class="doc-item-path">${escapeHtml(doc.sourcePath)}</div>`;
                      li.onclick = () => {
                        document.querySelectorAll('.doc-item').forEach(el => el.classList.remove('selected'));
                        li.classList.add('selected');
                        previewDocument(doc.sourcePath, doc.title);
                      };
                      container.appendChild(li);
                    });
                  } catch (e) {
                    container.innerHTML = `<li class="doc-item-path" style="padding:10px; color:var(--error-text)">Error loading corpus: ${escapeHtml(e.message)}</li>`;
                  }
                }

                async function previewDocument(path, title) {
                  const emptySec = document.getElementById('doc-viewer-empty');
                  const bodySec = document.getElementById('doc-viewer-body');
                  const titleEl = document.getElementById('doc-viewer-title');
                  const contentEl = document.getElementById('doc-viewer-content');
                  const badgeEl = document.getElementById('viewer-path-badge');

                  emptySec.style.display = 'none';
                  bodySec.style.display = 'block';
                  badgeEl.textContent = path;
                  titleEl.textContent = title;
                  contentEl.textContent = 'Loading text content...';

                  try {
                    const res = await fetch('api/knowledge/document?path=' + encodeURIComponent(path));
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);
                    contentEl.textContent = data.content;
                  } catch (e) {
                    contentEl.textContent = 'Error: ' + e.message;
                  }
                }

                async function runRagQuery() {
                  const query = document.getElementById('rag-input').value.trim();
                  if (!query) return;
                  const btn = document.getElementById('rag-submit-btn');
                  const panel = document.getElementById('rag-result-panel');
                  const errBanner = document.getElementById('rag-error-banner');
                  const ansEl = document.getElementById('rag-answer-output');
                  const srcContainer = document.getElementById('rag-sources-container');

                  btn.disabled = true;
                  btn.textContent = 'Processing Query...';
                  errBanner.style.display = 'none';
                  panel.style.display = 'block';
                  ansEl.textContent = 'Executing retrieval & generation pipeline...';
                  srcContainer.innerHTML = '';

                  try {
                    const res = await fetch('api/rag/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    ansEl.textContent = data.generatedAnswer;
                    if (data.sources && data.sources.length > 0) {
                      data.sources.forEach(src => {
                        const card = document.createElement('div');
                        card.style.cssText = 'background:var(--code-bg); border:1px solid var(--border-color); padding:10px 12px; border-radius:4px; margin-bottom:8px;';
                        card.innerHTML = `<div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-bottom:4px;">
                          <span>${escapeHtml(src.sourcePath)}</span>
                          <span class="badge badge-success">Relevance: ${(src.relevanceScore * 100).toFixed(1)}%</span>
                        </div><div style="font-size:0.825rem; font-family:var(--mono-font);">${escapeHtml(src.chunkText)}</div>`;
                        srcContainer.appendChild(card);
                      });
                    } else {
                      srcContainer.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">No matching source chunks retrieved.</div>';
                    }
                  } catch (e) {
                    panel.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'RAG Error: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Ask RAG Pipeline';
                  }
                }

                async function runAgentQuery() {
                  const query = document.getElementById('agent-input').value.trim();
                  if (!query) return;
                  const btn = document.getElementById('agent-submit-btn');
                  const panel = document.getElementById('agent-result-panel');
                  const errBanner = document.getElementById('agent-error-banner');
                  const ansEl = document.getElementById('agent-answer-output');
                  const traceContainer = document.getElementById('agent-trace-container');

                  btn.disabled = true;
                  btn.textContent = 'Agent Executing...';
                  errBanner.style.display = 'none';
                  panel.style.display = 'block';
                  ansEl.textContent = 'Evaluating agent decision loop...';
                  traceContainer.innerHTML = '';

                  try {
                    const res = await fetch('api/agent/query', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({query: query})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    ansEl.textContent = data.answer;
                    if (data.trace && data.trace.length > 0) {
                      data.trace.forEach(t => {
                        const stepDiv = document.createElement('div');
                        stepDiv.className = 'trace-step';
                        const numStr = String(t.step).padStart(2, '0');
                        const statusBadge = t.toolResultSuccess ? '<span class="badge badge-success">SUCCESS</span>' : '<span class="badge badge-error">RESULT</span>';
                        stepDiv.innerHTML = `
                          <div class="trace-step-num">${numStr}</div>
                          <div class="trace-step-body">
                            <div class="trace-step-header">
                              <strong>${escapeHtml(t.decisionType)}</strong>
                              <div>Tool: <code>${escapeHtml(t.toolName)}</code> &nbsp; ${statusBadge}</div>
                            </div>
                            <pre style="margin-top:4px; font-size:0.8rem;">${escapeHtml(t.toolOutput)}</pre>
                          </div>
                        `;
                        traceContainer.appendChild(stepDiv);
                      });
                    } else {
                      traceContainer.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">No trace steps recorded.</div>';
                    }
                  } catch (e) {
                    panel.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'Agent Failure: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.textContent = 'Run Controlled Agent';
                  }
                }

                function testMcpSecurityTraversal() {
                  document.getElementById('mcp-path-input').value = '../pom.xml';
                  runMcpTest();
                }

                async function runMcpTest() {
                  const path = document.getElementById('mcp-path-input').value.trim();
                  const btn = document.getElementById('mcp-submit-btn');
                  const panel = document.getElementById('mcp-result-panel');
                  const errBanner = document.getElementById('mcp-error-banner');
                  const toolsEl = document.getElementById('mcp-tools-output');
                  const invEl = document.getElementById('mcp-invocation-output');
                  const outcomeBadge = document.getElementById('mcp-outcome-badge');

                  btn.disabled = true;
                  btn.textContent = 'Executing MCP STDIO Test...';
                  errBanner.style.display = 'none';
                  panel.style.display = 'block';
                  toolsEl.textContent = 'Connecting to MCP Server over STDIO...';
                  invEl.textContent = '';

                  try {
                    const res = await fetch('api/mcp/test', {
                      method: 'POST',
                      headers: {'Content-Type': 'application/json'},
                      body: JSON.stringify({path: path})
                    });
                    const data = await res.json();
                    if (data.error) throw new Error(data.error);

                    toolsEl.textContent = JSON.stringify(data.discoveredTools || [], null, 2);
                    invEl.textContent = JSON.stringify(data.output || {}, null, 2);

                    if (data.success) {
                      outcomeBadge.className = 'badge badge-success';
                      outcomeBadge.textContent = 'SUCCESS';
                    } else {
                      outcomeBadge.className = 'badge badge-error';
                      outcomeBadge.textContent = 'REJECTED';
                    }
                  } catch (e) {
                    panel.style.display = 'none';
                    errBanner.style.display = 'block';
                    errBanner.textContent = 'MCP Error: ' + e.message;
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
