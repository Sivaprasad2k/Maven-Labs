/**
 * ODDLY SPECIFIC - Developer Observability Console Logic
 * Green-on-Black Real-time Interactive System
 */

(function () {
    'use strict';

    let activeTab = 'active'; // 'active', 'expired', 'system', 'config', 'about'
    let currentInspectedSessionId = null;
    let pollTimer = null;
    let clockTimer = null;

    // DOM Element Handles
    const elements = {
        tabActive: document.getElementById('tab-active'),
        tabExpired: document.getElementById('tab-expired'),
        tabSystem: document.getElementById('tab-system'),
        tabConfig: document.getElementById('tab-config'),
        tabAbout: document.getElementById('tab-about'),
        
        mainSessionsView: document.getElementById('main-sessions-view'),
        viewActive: document.getElementById('view-active'),
        viewExpired: document.getElementById('view-expired'),
        viewSystem: document.getElementById('view-system'),
        viewConfig: document.getElementById('view-config'),
        viewAbout: document.getElementById('view-about'),
        
        currentViewTitle: document.getElementById('current-view-title'),
        pillActive: document.getElementById('pill-active'),
        pillExpired: document.getElementById('pill-expired'),
        activeCountBadge: document.getElementById('active-count-badge'),
        sidebarActiveCount: document.getElementById('sidebar-active-count'),
        liveIndicator: document.getElementById('live-indicator'),
        clockDisplay: document.getElementById('clock-display'),
        
        activeEmpty: document.getElementById('active-empty-state'),
        activeWrapper: document.getElementById('active-table-wrapper'),
        activeTbody: document.getElementById('active-sessions-tbody'),
        
        expiredEmpty: document.getElementById('expired-empty-state'),
        expiredWrapper: document.getElementById('expired-table-wrapper'),
        expiredTbody: document.getElementById('expired-sessions-tbody'),
        
        detailPanel: document.getElementById('session-detail-panel'),
        detailEmptyState: document.getElementById('detail-empty-state'),
        detailActiveContent: document.getElementById('detail-active-content'),
        detailStatusPill: document.getElementById('detail-status-pill'),
        
        activityLog: document.getElementById('activity-log'),
        
        inspectorModal: document.getElementById('inspector-modal'),
        inspectorContent: document.getElementById('inspector-content'),
        btnCloseModal: document.getElementById('btn-close-modal')
    };

    function init() {
        bindEvents();
        startPolling();
        startClock();
        
        // Handle URL tab routing (/admin/expired)
        if (window.location.pathname.endsWith('/expired')) {
            switchTab('expired');
        }
    }

    function bindEvents() {
        if (elements.tabActive) elements.tabActive.addEventListener('click', () => switchTab('active'));
        if (elements.tabExpired) elements.tabExpired.addEventListener('click', () => switchTab('expired'));
        if (elements.tabSystem) elements.tabSystem.addEventListener('click', () => switchTab('system'));
        if (elements.tabConfig) elements.tabConfig.addEventListener('click', () => switchTab('config'));
        if (elements.tabAbout) elements.tabAbout.addEventListener('click', () => switchTab('about'));
        
        if (elements.btnCloseModal) elements.btnCloseModal.addEventListener('click', closeModal);
        if (elements.inspectorModal) {
            elements.inspectorModal.addEventListener('click', (e) => {
                if (e.target === elements.inspectorModal) closeModal();
            });
        }
    }

    function startClock() {
        const updateClock = () => {
            if (elements.clockDisplay) {
                const now = new Date();
                elements.clockDisplay.textContent = now.toLocaleTimeString();
            }
        };
        updateClock();
        clockTimer = setInterval(updateClock, 1000);
    }

    function switchTab(tab) {
        activeTab = tab;

        // Reset nav item active classes
        [elements.tabActive, elements.tabExpired, elements.tabSystem, elements.tabConfig, elements.tabAbout].forEach(el => {
            if (el) el.classList.remove('active');
        });

        // Hide standalone views
        [elements.viewSystem, elements.viewConfig, elements.viewAbout].forEach(el => {
            if (el) el.classList.add('hidden');
        });

        if (tab === 'active' || tab === 'expired') {
            if (elements.mainSessionsView) elements.mainSessionsView.classList.remove('hidden');

            if (tab === 'active') {
                if (elements.tabActive) elements.tabActive.classList.add('active');
                if (elements.viewActive) elements.viewActive.classList.add('active');
                if (elements.viewExpired) elements.viewExpired.classList.remove('active');
                if (elements.currentViewTitle) elements.currentViewTitle.textContent = 'ACTIVE SESSIONS';
            } else {
                if (elements.tabExpired) elements.tabExpired.classList.add('active');
                if (elements.viewExpired) elements.viewExpired.classList.add('active');
                if (elements.viewActive) elements.viewActive.classList.remove('active');
                if (elements.currentViewTitle) elements.currentViewTitle.textContent = 'EXPIRED SESSIONS';
            }
        } else {
            // Hide main sessions grid for standalone views
            if (elements.mainSessionsView) elements.mainSessionsView.classList.add('hidden');

            if (tab === 'system') {
                if (elements.tabSystem) elements.tabSystem.classList.add('active');
                if (elements.viewSystem) elements.viewSystem.classList.remove('hidden');
            } else if (tab === 'config') {
                if (elements.tabConfig) elements.tabConfig.classList.add('active');
                if (elements.viewConfig) elements.viewConfig.classList.remove('hidden');
            } else if (tab === 'about') {
                if (elements.tabAbout) elements.tabAbout.classList.add('active');
                if (elements.viewAbout) elements.viewAbout.classList.remove('hidden');
            }
        }
    }

    function startPolling() {
        fetchSessions();
        pollTimer = setInterval(fetchSessions, 2500);
    }

    async function fetchSessions() {
        try {
            const [activeRes, expiredRes] = await Promise.all([
                fetch('/api/admin/sessions'),
                fetch('/api/admin/sessions/expired')
            ]);

            if (!activeRes.ok || !expiredRes.ok) throw new Error('API request failed');

            const activeSessions = await activeRes.json();
            const expiredSessions = await expiredRes.json();

            updateUI(activeSessions, expiredSessions);

            if (currentInspectedSessionId) {
                refreshInspectedSession(activeSessions, expiredSessions);
            }

        } catch (err) {
            console.error('Polling error:', err);
            if (elements.liveIndicator) {
                elements.liveIndicator.innerHTML = '<span class="live-dot" style="background:var(--red-alert);"></span><span>DISCONNECTED</span>';
            }
        }
    }

    function updateUI(activeSessions, expiredSessions) {
        // Update Badges & Counts
        if (elements.pillActive) elements.pillActive.textContent = activeSessions.length;
        if (elements.pillExpired) elements.pillExpired.textContent = expiredSessions.length;
        if (elements.activeCountBadge) elements.activeCountBadge.textContent = `${activeSessions.length} active`;
        if (elements.sidebarActiveCount) elements.sidebarActiveCount.textContent = activeSessions.length;

        // 1. Render Active Sessions Table
        if (activeSessions.length === 0) {
            if (elements.activeEmpty) elements.activeEmpty.classList.remove('hidden');
            if (elements.activeWrapper) elements.activeWrapper.classList.add('hidden');
        } else {
            if (elements.activeEmpty) elements.activeEmpty.classList.add('hidden');
            if (elements.activeWrapper) elements.activeWrapper.classList.remove('hidden');
            renderActiveTable(activeSessions);
        }

        // 2. Render Expired Sessions Table
        if (expiredSessions.length === 0) {
            if (elements.expiredEmpty) elements.expiredEmpty.classList.remove('hidden');
            if (elements.expiredWrapper) elements.expiredWrapper.classList.add('hidden');
        } else {
            if (elements.expiredEmpty) elements.expiredEmpty.classList.add('hidden');
            if (elements.expiredWrapper) elements.expiredWrapper.classList.remove('hidden');
            renderExpiredTable(expiredSessions);
        }
    }

    function renderActiveTable(sessions) {
        if (!elements.activeTbody) return;
        elements.activeTbody.innerHTML = '';

        sessions.forEach(s => {
            const tr = document.createElement('tr');
            if (s.sessionId === currentInspectedSessionId) {
                tr.classList.add('selected');
            }

            const statusClass = s.status === 'COMPLETED' ? 'completed' : 'active';
            const shortId = s.sessionId.substring(0, 8) + '...';

            tr.innerHTML = `
                <td title="${s.sessionId}"><code>${shortId}</code></td>
                <td><code>${s.connectionIp}</code></td>
                <td>${s.challengeId || 'N/A'}</td>
                <td><span class="status-pill ${statusClass}">${s.status}</span></td>
                <td>${s.sessionAgeSeconds}s ago</td>
                <td>
                    <button class="btn-sm btn-inspect" data-id="${s.sessionId}">Inspect</button>
                    <button class="btn-sm danger btn-expire" data-id="${s.sessionId}">Expire</button>
                    <button class="btn-sm danger btn-delete" data-id="${s.sessionId}">Evict</button>
                </td>
            `;

            // Row Selection Listener
            tr.addEventListener('click', (e) => {
                if (e.target.tagName !== 'BUTTON') {
                    selectSession(s.sessionId, sessions);
                }
            });

            tr.querySelector('.btn-inspect').addEventListener('click', (e) => {
                e.stopPropagation();
                openInspector(s.sessionId);
            });
            tr.querySelector('.btn-expire').addEventListener('click', (e) => {
                e.stopPropagation();
                expireSession(s.sessionId);
            });
            tr.querySelector('.btn-delete').addEventListener('click', (e) => {
                e.stopPropagation();
                deleteSession(s.sessionId);
            });

            elements.activeTbody.appendChild(tr);
        });
    }

    function renderExpiredTable(sessions) {
        if (!elements.expiredTbody) return;
        elements.expiredTbody.innerHTML = '';

        sessions.forEach(s => {
            const tr = document.createElement('tr');
            if (s.sessionId === currentInspectedSessionId) {
                tr.classList.add('selected');
            }

            const shortId = s.sessionId.substring(0, 8) + '...';
            const locationSummary = s.ipCity ? `${s.ipCity}, ${s.ipCountry}` : 'Localhost Network';

            tr.innerHTML = `
                <td title="${s.sessionId}"><code>${shortId}</code></td>
                <td><code>${s.connectionIp}</code></td>
                <td>${locationSummary}</td>
                <td><span class="status-pill expired">EXPIRED</span></td>
                <td>${s.sessionAgeSeconds}s ago</td>
                <td>
                    <button class="btn-sm btn-inspect" data-id="${s.sessionId}">Inspect</button>
                    <button class="btn-sm danger btn-delete" data-id="${s.sessionId}">Delete</button>
                </td>
            `;

            tr.addEventListener('click', (e) => {
                if (e.target.tagName !== 'BUTTON') {
                    selectSession(s.sessionId, sessions);
                }
            });

            tr.querySelector('.btn-inspect').addEventListener('click', (e) => {
                e.stopPropagation();
                openInspector(s.sessionId);
            });
            tr.querySelector('.btn-delete').addEventListener('click', (e) => {
                e.stopPropagation();
                deleteSession(s.sessionId);
            });

            elements.expiredTbody.appendChild(tr);
        });
    }

    function selectSession(sessionId, sessionList) {
        currentInspectedSessionId = sessionId;
        const found = sessionList.find(s => s.sessionId === sessionId);
        if (found) {
            renderDetailPanel(found);
        }
    }

    async function openInspector(sessionId) {
        currentInspectedSessionId = sessionId;
        try {
            const res = await fetch(`/api/admin/sessions/${sessionId}`);
            if (!res.ok) throw new Error('Session not found');
            const data = await res.json();
            renderDetailPanel(data);
            
            // On mobile view (<768px), open modal as drawer fallback
            if (window.innerWidth <= 768) {
                renderInspectorModal(data);
                elements.inspectorModal.classList.remove('hidden');
            }
        } catch (err) {
            alert('Error fetching session details: ' + err.message);
        }
    }

    function refreshInspectedSession(activeSessions, expiredSessions) {
        const found = [...activeSessions, ...expiredSessions].find(s => s.sessionId === currentInspectedSessionId);
        if (found) {
            renderDetailPanel(found);
            if (elements.inspectorModal && !elements.inspectorModal.classList.contains('hidden')) {
                renderInspectorModal(found);
            }
        }
    }

    function renderDetailPanel(s) {
        if (!elements.detailActiveContent || !elements.detailEmptyState) return;

        elements.detailEmptyState.classList.add('hidden');
        elements.detailActiveContent.classList.remove('hidden');

        if (elements.detailStatusPill) {
            elements.detailStatusPill.textContent = s.status;
            elements.detailStatusPill.className = `status-pill ${s.status === 'EXPIRED' ? 'expired' : s.status === 'COMPLETED' ? 'completed' : 'active'}`;
        }

        let gpsText = '';
        if (s.locationGranted && s.latitude != null && s.longitude != null) {
            const mapUrl = `https://www.google.com/maps?q=${s.latitude},${s.longitude}`;
            gpsText = `
                <span style="color:var(--green-bright); font-weight:600;">EXACT GPS: ${s.latitude.toFixed(6)}, ${s.longitude.toFixed(6)}</span> (±${Math.round(s.accuracy || 10)}m)
                <br><a href="${mapUrl}" target="_blank" rel="noopener noreferrer" style="color:var(--cyan-info); text-decoration:underline; font-size:0.72rem;">[ View Coordinates on Map ]</a>
            `;
        } else if (s.ipLatitude != null && s.ipLongitude != null) {
            const mapUrl = `https://www.google.com/maps?q=${s.ipLatitude},${s.ipLongitude}`;
            gpsText = `
                <span style="color:var(--amber-warning);">Precise GPS unavailable / denied</span>
                <br><span style="color:var(--cyan-info); font-size:0.72rem;">IP Fallback: ${s.ipLatitude.toFixed(4)}, ${s.ipLongitude.toFixed(4)} (${s.ipCity || ''}, ${s.ipCountry || ''})</span>
            `;
        } else {
            gpsText = '<span style="color:var(--amber-warning);">Precise GPS unavailable / denied</span>';
        }

        const ipLocText = s.ipCity ? `${s.ipCity}, ${s.ipRegion || ''} ${s.ipCountry || ''}` : 'Localhost Network Loopback';

        elements.detailActiveContent.innerHTML = `
            <div class="inspect-section">
                <div class="inspect-title">SESSION DETAILS</div>
                <div class="inspect-grid">
                    <div class="inspect-item"><div class="lbl">SESSION ID</div><div class="val" title="${s.sessionId}">${s.sessionId.substring(0, 12)}...</div></div>
                    <div class="inspect-item"><div class="lbl">STATUS</div><div class="val">${s.status}</div></div>
                    <div class="inspect-item"><div class="lbl">CHALLENGE</div><div class="val">${s.challengeId || 'N/A'}</div></div>
                    <div class="inspect-item"><div class="lbl">SESSION AGE</div><div class="val">${s.sessionAgeSeconds}s ago</div></div>
                </div>
            </div>

            <div class="inspect-section">
                <div class="inspect-title">CONNECTION DETAILS</div>
                <div class="inspect-grid">
                    <div class="inspect-item"><div class="lbl">CONNECTION IP</div><div class="val">${s.connectionIp}</div></div>
                    <div class="inspect-item"><div class="lbl">NETWORK / ISP</div><div class="val">${s.isp || 'Local Network'}</div></div>
                </div>
            </div>

            <div class="inspect-section">
                <div class="inspect-title">LOCATION DETAILS</div>
                <div class="inspect-grid">
                    <div class="inspect-item"><div class="lbl">IP GEOLOCATION</div><div class="val">${ipLocText}</div></div>
                    <div class="inspect-item"><div class="lbl">BROWSER GPS</div><div class="val">${gpsText}</div></div>
                </div>
            </div>

            <div class="inspect-section">
                <div class="inspect-title">CLIENT ENVIRONMENT</div>
                <div class="inspect-grid">
                    <div class="inspect-item"><div class="lbl">PLATFORM / OS</div><div class="val">${s.platform || 'Unknown'}</div></div>
                    <div class="inspect-item"><div class="lbl">TIMEZONE</div><div class="val">${s.timezone || 'UTC'}</div></div>
                    <div class="inspect-item"><div class="lbl">LANGUAGE</div><div class="val">${s.language || 'en-US'}</div></div>
                    <div class="inspect-item"><div class="lbl">RESOLUTION</div><div class="val">${s.screenResolution || 'Unknown'}</div></div>
                </div>
            </div>

            <div class="detail-actions">
                <button class="btn-detail-act btn-expire-act" type="button" data-id="${s.sessionId}">EXPIRE SESSION</button>
                <button class="btn-detail-act btn-evict-act" type="button" data-id="${s.sessionId}">EVICT SESSION</button>
            </div>
        `;

        const btnExp = elements.detailActiveContent.querySelector('.btn-expire-act');
        const btnEv = elements.detailActiveContent.querySelector('.btn-evict-act');

        if (btnExp) btnExp.addEventListener('click', () => expireSession(s.sessionId));
        if (btnEv) btnEv.addEventListener('click', () => deleteSession(s.sessionId));
    }

    function renderInspectorModal(s) {
        if (!elements.inspectorContent) return;
        renderDetailPanel(s);
        elements.inspectorContent.innerHTML = elements.detailActiveContent.innerHTML;
    }

    async function expireSession(sessionId) {
        if (!confirm(`Manually expire session ${sessionId.substring(0, 8)}?`)) return;
        try {
            await fetch(`/api/admin/sessions/${sessionId}/expire`, { method: 'POST' });
            logActivity(`Session ${sessionId.substring(0, 8)} manually expired by developer.`);
            fetchSessions();
        } catch (err) {
            alert('Error expiring session');
        }
    }

    async function deleteSession(sessionId) {
        if (!confirm(`Evict session ${sessionId.substring(0, 8)}?`)) return;
        try {
            await fetch(`/api/admin/sessions/${sessionId}`, { method: 'DELETE' });
            logActivity(`Session ${sessionId.substring(0, 8)} evicted from memory store.`);
            if (currentInspectedSessionId === sessionId) {
                currentInspectedSessionId = null;
                if (elements.detailActiveContent) elements.detailActiveContent.classList.add('hidden');
                if (elements.detailEmptyState) elements.detailEmptyState.classList.remove('hidden');
                closeModal();
            }
            fetchSessions();
        } catch (err) {
            alert('Error deleting session');
        }
    }

    function logActivity(msg) {
        if (!elements.activityLog) return;
        const entry = document.createElement('div');
        entry.className = 'log-entry';
        const now = new Date().toLocaleTimeString();
        entry.innerHTML = `
            <span class="log-time">[${now}]</span>
            <span class="log-msg">${msg}</span>
        `;
        elements.activityLog.prepend(entry);
    }

    function closeModal() {
        if (elements.inspectorModal) {
            elements.inspectorModal.classList.add('hidden');
        }
    }

    document.addEventListener('DOMContentLoaded', init);

})();
