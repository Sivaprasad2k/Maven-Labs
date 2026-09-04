package com.shevay.knowledge.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet serving the unified Developer Console at GET / and GET /console.
 * Designed with a clean, high-density developer console aesthetic matching the reference layout.
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
                  --bg-app: #f8fafc;
                  --sidebar-bg: #0f172a;
                  --sidebar-border: #1e293b;
                  --sidebar-text: #94a3b8;
                  --sidebar-text-active: #ffffff;
                  --sidebar-active-bg: #0969da;
                  --surface-card: #ffffff;
                  --surface-subtle: #f1f5f9;
                  --border-color: #e2e8f0;
                  --border-dark: #cbd5e1;
                  --text-primary: #0f172a;
                  --text-muted: #64748b;
                  --text-code: #0f172a;
                  --accent: #0969da;
                  --accent-hover: #0353b4;
                  --accent-light: #eff6ff;
                  --success: #16a34a;
                  --success-bg: #f0fdf4;
                  --error: #dc2626;
                  --error-bg: #fef2f2;
                  --warning: #d97706;
                  --warning-bg: #fffbeb;
                  --mono-font: "JetBrains Mono", "IBM Plex Mono", SFMono-Regular, Consolas, monospace;
                  --sans-font: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                  --radius: 12px;
                  --radius-sm: 8px;
                  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
                  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
                }

                * { box-sizing: border-box; margin: 0; padding: 0; }
                body {
                  font-family: var(--sans-font);
                  background-color: var(--bg-app);
                  color: var(--text-primary);
                  line-height: 1.5;
                  font-size: 0.9rem;
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

                .brand-title-wrap {
                  display: flex;
                  align-items: center;
                  gap: 10px;
                  color: #ffffff;
                  font-weight: 700;
                  font-size: 1.05rem;
                }

                .brand-icon {
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  width: 34px;
                  height: 34px;
                  background-color: var(--accent);
                  border-radius: var(--radius-sm);
                  color: #ffffff;
                }

                .brand-subtitle {
                  font-size: 0.75rem;
                  color: var(--sidebar-text);
                  margin-top: 6px;
                }

                .brand-badge {
                  display: inline-block;
                  font-size: 0.65rem;
                  background-color: #1e293b;
                  color: #38bdf8;
                  padding: 2px 8px;
                  border-radius: 12px;
                  font-weight: 600;
                  margin-top: 8px;
                  letter-spacing: 0.05em;
                }

                .nav-menu {
                  list-style: none;
                  display: flex;
                  flex-direction: column;
                  gap: 6px;
                }

                .tab-btn {
                  width: 100%;
                  text-align: left;
                  background: none;
                  border: none;
                  color: var(--sidebar-text);
                  padding: 10px 14px;
                  border-radius: var(--radius-sm);
                  font-size: 0.875rem;
                  font-weight: 500;
                  cursor: pointer;
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  transition: all 0.15s ease;
                }

                .tab-btn:hover {
                  background-color: #1e293b;
                  color: #ffffff;
                }

                .tab-btn.active {
                  background-color: var(--sidebar-active-bg);
                  color: var(--sidebar-text-active);
                  font-weight: 600;
                  box-shadow: 0 2px 4px rgba(9, 105, 218, 0.3);
                }

                .sidebar-footer {
                  padding-top: 16px;
                  border-top: 1px solid var(--sidebar-border);
                  font-size: 0.75rem;
                  color: #64748b;
                }

                .sidebar-footer-title {
                  color: #94a3b8;
                  font-weight: 600;
                }

                /* Main Content Stage */
                .main-stage {
                  flex: 1;
                  padding: 32px;
                  overflow-y: auto;
                  max-width: 1280px;
                }

                /* Top Header */
                .top-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: flex-start;
                  margin-bottom: 24px;
                }

                .header-title-section h1 {
                  font-size: 1.5rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  letter-spacing: -0.02em;
                }

                .header-title-section p {
                  font-size: 0.875rem;
                  color: var(--text-muted);
                  margin-top: 2px;
                }

                .header-actions {
                  display: flex;
                  align-items: center;
                  gap: 12px;
                }

                .status-badge-ready {
                  display: inline-flex;
                  align-items: center;
                  gap: 6px;
                  font-size: 0.75rem;
                  font-weight: 600;
                  color: var(--success);
                  background-color: var(--success-bg);
                  border: 1px solid rgba(22, 163, 74, 0.2);
                  padding: 4px 10px;
                  border-radius: 20px;
                }

                .dot-green {
                  width: 7px;
                  height: 7px;
                  border-radius: 50%;
                  background-color: var(--success);
                }

                .icon-btn {
                  background: var(--surface-card);
                  border: 1px solid var(--border-color);
                  color: var(--text-muted);
                  width: 32px;
                  height: 32px;
                  border-radius: 50%;
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  cursor: pointer;
                  transition: all 0.15s ease;
                }

                .icon-btn:hover {
                  color: var(--text-primary);
                  border-color: var(--border-dark);
                  background: var(--surface-subtle);
                }

                /* Metric Strip */
                .metric-strip {
                  display: grid;
                  grid-template-columns: repeat(5, 1fr);
                  gap: 14px;
                  margin-bottom: 24px;
                }

                .metric-box {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 14px 16px;
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  box-shadow: var(--shadow-sm);
                }

                .metric-icon-wrap {
                  width: 36px;
                  height: 36px;
                  border-radius: var(--radius-sm);
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  background-color: var(--surface-subtle);
                  flex-shrink: 0;
                }

                .metric-info {
                  overflow: hidden;
                }

                .metric-lbl {
                  font-size: 0.725rem;
                  color: var(--text-muted);
                  font-weight: 500;
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

                /* Card Panel */
                .card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 24px;
                  margin-bottom: 24px;
                  box-shadow: var(--shadow-sm);
                }

                .card-title-row {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 16px;
                }

                .card-h2 {
                  font-size: 1.1rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  display: flex;
                  align-items: center;
                  gap: 10px;
                }

                .card-desc {
                  font-size: 0.85rem;
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
                  padding: 12px 16px;
                  font-size: 0.9rem;
                  font-family: var(--sans-font);
                  background-color: #ffffff;
                  border: 1px solid var(--border-dark);
                  border-radius: var(--radius-sm);
                  color: var(--text-primary);
                  transition: all 0.15s ease;
                }

                .text-input:focus {
                  outline: none;
                  border-color: var(--accent);
                  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
                }

                .btn-primary {
                  background-color: var(--accent);
                  color: #ffffff;
                  border: none;
                  padding: 12px 22px;
                  font-size: 0.875rem;
                  font-weight: 600;
                  border-radius: var(--radius-sm);
                  cursor: pointer;
                  display: inline-flex;
                  align-items: center;
                  gap: 8px;
                  transition: background-color 0.15s ease;
                  white-space: nowrap;
                }

                .btn-primary:hover {
                  background-color: var(--accent-hover);
                }

                .btn-primary:disabled {
                  opacity: 0.65;
                  cursor: not-allowed;
                }

                .btn-secondary {
                  background-color: #ffffff;
                  color: var(--text-primary);
                  border: 1px solid var(--border-dark);
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
                  background-color: var(--surface-subtle);
                  border-color: #94a3b8;
                }

                /* Suggestion Chips */
                .suggestions-row {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  flex-wrap: wrap;
                  margin-top: 12px;
                }

                .suggestions-lbl {
                  font-size: 0.775rem;
                  color: var(--text-muted);
                  font-weight: 500;
                  margin-right: 4px;
                }

                .chip-btn {
                  background-color: #f1f5f9;
                  border: 1px solid #e2e8f0;
                  color: #2563eb;
                  padding: 5px 12px;
                  border-radius: 20px;
                  font-size: 0.775rem;
                  font-weight: 500;
                  cursor: pointer;
                  transition: all 0.15s ease;
                }

                .chip-btn:hover {
                  background-color: #e0e7ff;
                  border-color: #bfdbfe;
                  color: #1d4ed8;
                }

                /* Empty State Card */
                .empty-state-card {
                  text-align: center;
                  padding: 48px 32px;
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  margin-bottom: 24px;
                }

                .empty-icon-box {
                  width: 64px;
                  height: 64px;
                  background-color: #eff6ff;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  margin: 0 auto 16px auto;
                  color: var(--accent);
                }

                .empty-state-card h3 {
                  font-size: 1.2rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  margin-bottom: 8px;
                }

                .empty-state-card p {
                  font-size: 0.875rem;
                  color: var(--text-muted);
                  max-width: 540px;
                  margin: 0 auto 32px auto;
                  line-height: 1.6;
                }

                .feature-grid {
                  display: grid;
                  grid-template-columns: repeat(4, 1fr);
                  gap: 16px;
                  text-align: left;
                }

                .feature-item {
                  background-color: var(--bg-app);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 16px;
                  display: flex;
                  gap: 12px;
                  align-items: flex-start;
                }

                .feature-icon-circle {
                  width: 32px;
                  height: 32px;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  flex-shrink: 0;
                }

                .feature-title {
                  font-size: 0.825rem;
                  font-weight: 700;
                  color: var(--text-primary);
                  margin-bottom: 2px;
                }

                .feature-desc {
                  font-size: 0.75rem;
                  color: var(--text-muted);
                  line-height: 1.4;
                }

                /* Results Stage */
                .result-block {
                  margin-bottom: 24px;
                }

                .result-section-label {
                  font-size: 0.75rem;
                  font-weight: 700;
                  letter-spacing: 0.08em;
                  color: var(--text-muted);
                  text-transform: uppercase;
                  margin-bottom: 10px;
                }

                .answer-box {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius);
                  padding: 20px;
                  font-size: 0.925rem;
                  color: var(--text-primary);
                  line-height: 1.7;
                  box-shadow: var(--shadow-sm);
                }

                .source-row-card {
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 16px;
                  margin-bottom: 12px;
                  box-shadow: var(--shadow-sm);
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
                  color: var(--accent);
                }

                .sim-badge {
                  font-family: var(--mono-font);
                  font-size: 0.725rem;
                  font-weight: 600;
                  background-color: var(--success-bg);
                  color: var(--success);
                  border: 1px solid rgba(22, 163, 74, 0.2);
                  padding: 2px 8px;
                  border-radius: 12px;
                }

                .chunk-snippet {
                  background-color: var(--bg-app);
                  border: 1px solid var(--border-color);
                  border-radius: 6px;
                  padding: 12px;
                  font-family: var(--mono-font);
                  font-size: 0.8rem;
                  color: var(--text-code);
                  white-space: pre-wrap;
                  word-break: break-word;
                }

                /* Expandable Details */
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

                /* Document List Table */
                .doc-table {
                  width: 100%;
                  border-collapse: collapse;
                  margin-top: 8px;
                }

                .doc-table th {
                  text-align: left;
                  font-size: 0.75rem;
                  font-weight: 700;
                  text-transform: uppercase;
                  letter-spacing: 0.05em;
                  color: var(--text-muted);
                  padding: 10px 16px;
                  border-bottom: 1px solid var(--border-color);
                }

                .doc-table td {
                  padding: 14px 16px;
                  border-bottom: 1px solid var(--border-color);
                  font-size: 0.875rem;
                }

                .doc-table tr:hover td {
                  background-color: var(--surface-subtle);
                  cursor: pointer;
                }

                /* Agent Trace Timeline */
                .trace-timeline {
                  display: flex;
                  flex-direction: column;
                  gap: 16px;
                  margin-top: 16px;
                  position: relative;
                  padding-left: 24px;
                  border-left: 2px solid var(--border-color);
                }

                .trace-node {
                  position: relative;
                  background-color: var(--surface-card);
                  border: 1px solid var(--border-color);
                  border-radius: var(--radius-sm);
                  padding: 16px;
                  box-shadow: var(--shadow-sm);
                }

                .trace-node::before {
                  content: '';
                  position: absolute;
                  left: -31px;
                  top: 20px;
                  width: 12px;
                  height: 12px;
                  border-radius: 50%;
                  background-color: var(--accent);
                  border: 2px solid #ffffff;
                }

                .trace-node-header {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 8px;
                }

                .trace-node-title {
                  font-weight: 700;
                  font-size: 0.875rem;
                  color: var(--text-primary);
                }

                /* Banner Error */
                .banner-error {
                  background-color: var(--error-bg);
                  border: 1px solid rgba(220, 38, 38, 0.2);
                  color: var(--error);
                  padding: 14px 18px;
                  border-radius: var(--radius-sm);
                  font-size: 0.875rem;
                  margin-bottom: 20px;
                  display: none;
                }

                /* Page Tab Views */
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
                  font-size: 0.775rem;
                  color: var(--text-muted);
                }

                .page-footer a {
                  color: var(--text-muted);
                  text-decoration: none;
                  margin-left: 12px;
                }

                .page-footer a:hover {
                  color: var(--accent);
                }

                /* Responsive */
                @media (max-width: 1024px) {
                  .metric-strip { grid-template-columns: repeat(3, 1fr); }
                  .feature-grid { grid-template-columns: repeat(2, 1fr); }
                }
                @media (max-width: 768px) {
                  .app-layout { flex-direction: column; }
                  .sidebar { width: 100%; border-right: none; border-bottom: 1px solid var(--sidebar-border); }
                  .main-stage { padding: 20px; }
                  .metric-strip { grid-template-columns: repeat(2, 1fr); }
                  .feature-grid { grid-template-columns: 1fr; }
                  .top-header { flex-direction: column; gap: 12px; }
                }
              </style>
            </head>
            <body>
              <div class="app-layout">
                <!-- Sidebar -->
                <aside class="sidebar">
                  <div>
                    <div class="brand-header">
                      <div class="brand-title-wrap">
                        <div class="brand-icon">
                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
                        </div>
                        <div>
                          <div>Maven Knowledge Lab</div>
                          <div class="brand-subtitle">Developer Knowledge Assistant</div>
                        </div>
                      </div>
                      <span class="brand-badge">Phase 8</span>
                    </div>

                    <ul class="nav-menu">
                      <li>
                        <button class="tab-btn active" id="nav-rag" onclick="switchView('rag')">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                          RAG Pipeline
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-knowledge" onclick="switchView('knowledge')">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
                          Knowledge Base
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-agent" onclick="switchView('agent')">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                          Knowledge Agent
                        </button>
                      </li>
                      <li>
                        <button class="tab-btn" id="nav-mcp" onclick="switchView('mcp')">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="18" cy="18" r="3"/><circle cx="6" cy="6" r="3"/><path d="M13 6h3a2 2 0 0 1 2 2v7"/><line x1="6" y1="9" x2="6" y2="21"/></svg>
                          MCP Protocol
                        </button>
                      </li>
                    </ul>
                  </div>

                  <div class="sidebar-footer">
                    <div class="sidebar-footer-title">Maven Knowledge Lab</div>
                    <div>Build 1.0.0</div>
                  </div>
                </aside>

                <!-- Main Content Stage -->
                <main class="main-stage">
                  <!-- Top Bar Header -->
                  <div class="top-header">
                    <div class="header-title-section">
                      <h1 id="page-title">RAG Pipeline</h1>
                      <p id="page-subtitle">Retrieve relevant knowledge and generate grounded answers.</p>
                    </div>
                    <div class="header-actions">
                      <div class="status-badge-ready">
                        <span class="dot-green"></span>
                        System Ready
                      </div>
                      <button class="icon-btn" title="Settings">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                      </button>
                      <button class="icon-btn" title="Help & Documentation">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                      </button>
                    </div>
                  </div>

                  <!-- Metric Strip -->
                  <div class="metric-strip">
                    <div class="metric-box">
                      <div class="metric-icon-wrap" style="background:#f0fdf4;">
                        <span class="dot-green"></span>
                      </div>
                      <div class="metric-info">
                        <div class="metric-lbl">Runtime</div>
                        <div class="metric-val-text" style="color:#16a34a;">Online</div>
                      </div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-icon-wrap" style="background:#eff6ff;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"/><rect x="9" y="9" width="6" height="6"/><line x1="9" y1="1" x2="9" y2="4"/><line x1="15" y1="1" x2="15" y2="4"/><line x1="9" y1="20" x2="9" y2="23"/><line x1="15" y1="20" x2="15" y2="23"/><line x1="20" y1="9" x2="23" y2="9"/><line x1="20" y1="15" x2="23" y2="15"/><line x1="1" y1="9" x2="4" y2="9"/><line x1="1" y1="15" x2="4" y2="15"/></svg>
                      </div>
                      <div class="metric-info">
                        <div class="metric-lbl">LLM Model</div>
                        <div class="metric-val-text">gemini-3.6-flash</div>
                      </div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-icon-wrap" style="background:#faf5ff;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#9333ea" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
                      </div>
                      <div class="metric-info">
                        <div class="metric-lbl">Vector Dimension</div>
                        <div class="metric-val-text">768</div>
                      </div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-icon-wrap" style="background:#fff7ed;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ea580c" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                      </div>
                      <div class="metric-info">
                        <div class="metric-lbl">Knowledge Documents</div>
                        <div class="metric-val-text" id="stat-docs">4</div>
                      </div>
                    </div>

                    <div class="metric-box">
                      <div class="metric-icon-wrap" style="background:#f0fdfa;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0d9488" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
                      </div>
                      <div class="metric-info">
                        <div class="metric-lbl">Vector Records</div>
                        <div class="metric-val-text" id="stat-vectors">5</div>
                      </div>
                    </div>
                  </div>

                  <!-- VIEW 1: RAG PIPELINE VIEW -->
                  <div id="pane-rag" class="tab-pane active">
                    <div class="card">
                      <div class="card-h2">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0969da" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                        Ask a Question
                      </div>
                      <div class="card-desc">Search the Maven knowledge base and get a grounded answer.</div>

                      <div class="search-input-group">
                        <input type="text" id="rag-query-input" class="text-input" placeholder="What would you like to know about Maven?" value="What is the Maven lifecycle?">
                        <button class="btn-primary" id="rag-run-btn" onclick="executeRagSearch()">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
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

                    <!-- RAG Empty State (Before execution) -->
                    <div id="rag-empty-state" class="empty-state-card">
                      <div class="empty-icon-box">
                        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
                      </div>
                      <h3>Get started with the RAG Pipeline</h3>
                      <p>Enter a question above to search your Maven knowledge base. The system will retrieve relevant content and generate an accurate answer using Gemini AI with source citations.</p>

                      <div class="feature-grid">
                        <div class="feature-item">
                          <div class="feature-icon-circle" style="background:#eff6ff; color:#2563eb;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                          </div>
                          <div>
                            <div class="feature-title">Semantic Search</div>
                            <div class="feature-desc">Finds the most relevant knowledge using vector search</div>
                          </div>
                        </div>

                        <div class="feature-item">
                          <div class="feature-icon-circle" style="background:#f0fdf4; color:#16a34a;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                          </div>
                          <div>
                            <div class="feature-title">Source Citations</div>
                            <div class="feature-desc">Shows exact source documents for every answer</div>
                          </div>
                        </div>

                        <div class="feature-item">
                          <div class="feature-icon-circle" style="background:#fdf4ff; color:#c026d3;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                          </div>
                          <div>
                            <div class="feature-title">Accurate Answers</div>
                            <div class="feature-desc">Powered by Gemini 3.6 Flash grounded in your knowledge</div>
                          </div>
                        </div>

                        <div class="feature-item">
                          <div class="feature-icon-circle" style="background:#fff7ed; color:#ea580c;">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                          </div>
                          <div>
                            <div class="feature-title">Fast & Reliable</div>
                            <div class="feature-desc">Get answers in seconds</div>
                          </div>
                        </div>
                      </div>
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
                          <tr><td class="lbl">Embedding Provider</td><td class="val">GeminiEmbeddingProvider</td></tr>
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
                          <div class="card-desc">Indexed technical documentation used by the assistant.</div>
                        </div>
                        <button class="btn-secondary" onclick="fetchDocs()">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
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
                      <div class="card-h2">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0969da" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                        Knowledge Agent
                      </div>
                      <div class="card-desc">Agentic reasoning over the Maven knowledge base.</div>

                      <div class="search-input-group">
                        <input type="text" id="agent-query-input" class="text-input" placeholder="What would you like the agent to investigate?" value="Explain Maven dependency scopes">
                        <button class="btn-primary" id="agent-run-btn" onclick="executeAgentLoop()">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="5 3 19 12 5 21 5 3"/></svg>
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
                          <div class="card-desc">Inspect the MCP client-server tool connection.</div>
                        </div>
                        <span class="sim-badge" style="background:#f0fdf4; color:#16a34a; font-size:0.8rem;">CONNECTED</span>
                      </div>

                      <div style="display: flex; gap: 10px; align-items: center; background:var(--bg-app); padding:12px 16px; border-radius:var(--radius-sm); border:1px solid var(--border-color); font-family:var(--mono-font); font-size:0.8rem; margin-bottom: 20px;">
                        <span>Browser</span> &rarr;
                        <span style="color:var(--accent); font-weight:600;">MCP Client</span> &rarr;
                        <span style="color:#9333ea; font-weight:600;">STDIO</span> &rarr;
                        <span style="color:#ea580c; font-weight:600;">MCP Server</span> &rarr;
                        <span style="color:#16a34a; font-weight:600;">Tool</span>
                      </div>

                      <details class="exec-details" open>
                        <summary style="font-size:0.875rem;">Tool: getKnowledgeDocument</summary>
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
                      <div class="card-h2" style="font-size:1rem; margin-bottom:12px;">Tool Invocation</div>
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
                        <div class="card-h2" style="font-size:0.95rem;">Result Output</div>
                        <span class="sim-badge" id="mcp-status-badge">SUCCESS</span>
                      </div>
                      <div class="chunk-snippet" id="mcp-output-body">Awaiting response...</div>
                    </div>
                  </div>

                  <!-- Footer -->
                  <footer class="page-footer">
                    <div>Maven Knowledge Lab &middot; Developer Productivity with AI</div>
                    <div>
                      <a href="#">Documentation</a> |
                      <a href="#">GitHub</a> |
                      <span>v1.0.0</span>
                    </div>
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
                    'rag': { title: 'RAG Pipeline', sub: 'Retrieve relevant knowledge and generate grounded answers.' },
                    'knowledge': { title: 'Knowledge Base', sub: 'Indexed technical documentation used by the assistant.' },
                    'agent': { title: 'Knowledge Agent', sub: 'Agentic reasoning over the Maven knowledge base.' },
                    'mcp': { title: 'MCP Protocol', sub: 'Inspect the MCP client-server tool connection.' }
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
                  btn.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"/><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/></svg> Searching...';
                  errBox.style.display = 'none';
                  emptyState.style.display = 'none';
                  stage.style.display = 'block';
                  answerBody.textContent = 'Searching vector store and generating answer with Gemini 3.6 Flash...';
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
                            <span class="sim-badge">Similarity ${chunk.relevanceScore.toFixed(2)}</span>
                          </div>
                          <div class="chunk-snippet">${escapeHtml(chunk.chunkText)}</div>
                        `;
                        sourcesContainer.appendChild(card);
                      });
                    } else {
                      chunkCountVal.textContent = '0';
                      sourcesContainer.innerHTML = '<div style="font-size:0.85rem; color:var(--text-muted); padding:16px; background:var(--surface-card); border:1px solid var(--border-color); border-radius:var(--radius-sm);">No sources matched similarity threshold (&ge; 0.70).</div>';
                    }
                  } catch (e) {
                    stage.style.display = 'none';
                    emptyState.style.display = 'block';
                    errBox.style.display = 'block';
                    errBox.textContent = 'Search failed: ' + e.message;
                  } finally {
                    btn.disabled = false;
                    btn.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg> Run Search';
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
                    userNode.innerHTML = `<div class="trace-node-header"><span class="trace-node-title">User Query</span></div><div style="font-size:0.85rem; color:var(--text-primary);">${escapeHtml(query)}</div>`;
                    traceList.appendChild(userNode);

                    if (data.trace && data.trace.length > 0) {
                      data.trace.forEach(t => {
                        const stepNode = document.createElement('div');
                        stepNode.className = 'trace-node';
                        const badge = t.toolResultSuccess
                          ? '<span class="sim-badge" style="background:#f0fdf4; color:#16a34a;">Status: Success</span>'
                          : '<span class="sim-badge" style="background:#fef2f2; color:#dc2626;">Status: Output</span>';
                        stepNode.innerHTML = `
                          <div class="trace-node-header">
                            <span class="trace-node-title">Step ${t.step}: Tool Call</span>
                            <div><code style="font-family:var(--mono-font); font-weight:600; color:var(--accent);">${escapeHtml(t.toolName)}</code> &nbsp; ${badge}</div>
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
                    finalNode.innerHTML = `<div class="trace-node-header"><span class="trace-node-title">Step ${data.trace ? data.trace.length + 1 : 1}: Final Answer</span></div><div style="font-size:0.85rem; color:var(--text-primary);">${escapeHtml(data.answer)}</div>`;
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
                      badge.style.cssText = 'background:#f0fdf4; color:#16a34a;';
                      badge.textContent = 'SUCCESS';
                    } else {
                      badge.className = 'sim-badge';
                      badge.style.cssText = 'background:#fef2f2; color:#dc2626;';
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
