/**
 * ODDLY SPECIFIC - Frontend Application & Interactive State Machine
 */

(function () {
    'use strict';

    // Application State Registry
    const STATES = {
        LANDING: 'state-landing',
        CHALLENGE: 'state-challenge',
        ANALYSIS: 'state-analysis',
        REVEAL: 'state-reveal',
        PRANK: 'state-prank',
        EXPLANATION: 'state-explanation'
    };

    // State Memory Variables
    let currentState = STATES.LANDING;
    let sessionId = null;
    let selectedChallenge = null;

    // DOM Element Handles
    const elements = {
        btnStart: document.getElementById('btn-start'),
        geoStatus: document.getElementById('geo-status'),
        sessionTag: document.getElementById('session-tag'),
        
        challengeTypeTag: document.getElementById('challenge-type-tag'),
        challengeTitle: document.getElementById('challenge-title'),
        challengeInstructions: document.getElementById('challenge-instructions'),
        challengeInteractiveArea: document.getElementById('challenge-interactive-area'),
        
        analysisProgress: document.getElementById('analysis-progress'),
        analysisPercent: document.getElementById('analysis-percent'),
        analysisFeed: document.getElementById('analysis-feed'),
        
        revealIp: document.getElementById('reveal-ip'),
        revealLocation: document.getElementById('reveal-location'),
        revealStatus: document.getElementById('reveal-status'),
        
        btnExplain: document.getElementById('btn-explain'),
        btnRestart: document.getElementById('btn-restart')
    };

    // Initialize Application
    function init() {
        bindEvents();
        switchState(STATES.LANDING);
    }

    function bindEvents() {
        if (elements.btnStart) {
            elements.btnStart.addEventListener('click', handleStartExperience);
        }
        if (elements.btnExplain) {
            elements.btnExplain.addEventListener('click', () => switchState(STATES.EXPLANATION));
        }
        if (elements.btnRestart) {
            elements.btnRestart.addEventListener('click', () => {
                sessionId = null;
                selectedChallenge = null;
                if (elements.sessionTag) elements.sessionTag.textContent = 'SESSION :: OFFLINE';
                if (elements.geoStatus) elements.geoStatus.textContent = '';
                switchState(STATES.LANDING);
            });
        }
    }

    // State Machine Switcher with Personality Transitions
    function switchState(targetState) {
        currentState = targetState;
        document.querySelectorAll('.stage-view').forEach(el => {
            el.classList.remove('active');
        });
        const targetEl = document.getElementById(targetState);
        if (targetEl) {
            targetEl.classList.add('active');
        }
    }

    // Flow Step 1: Start Experience & Capture Browser GPS + Session Metadata
    function handleStartExperience() {
        elements.btnStart.disabled = true;

        const clientEnv = {
            locationGranted: false,
            latitude: null,
            longitude: null,
            accuracy: null,
            userAgent: navigator.userAgent || 'Unknown Browser',
            platform: navigator.platform || 'Unknown Platform',
            language: navigator.language || 'en-US',
            timezone: (Intl && Intl.DateTimeFormat) ? Intl.DateTimeFormat().resolvedOptions().timeZone : 'UTC',
            screenResolution: (window.screen) ? `${window.screen.width}x${window.screen.height}` : 'Unknown'
        };

        const attemptGeolocation = (highAccuracy, timeoutMs) => {
            return new Promise((resolve) => {
                if (!('geolocation' in navigator)) {
                    resolve(null);
                    return;
                }
                navigator.geolocation.getCurrentPosition(
                    (pos) => resolve(pos.coords),
                    (err) => {
                        console.warn(`Browser GPS attempt (highAccuracy=${highAccuracy}, code=${err.code}): ${err.message}`);
                        resolve(null);
                    },
                    {
                        enableHighAccuracy: highAccuracy,
                        timeout: timeoutMs,
                        maximumAge: 60000
                    }
                );
            });
        };

        (async () => {
            // Attempt 1: High accuracy mode (fast timeout 3.5s)
            let coords = await attemptGeolocation(true, 3500);

            // Attempt 2: Standard/Wi-Fi accuracy fallback (timeout 4.5s) - essential for desktop browsers without hardware GPS
            if (!coords) {
                coords = await attemptGeolocation(false, 4500);
            }

            if (coords) {
                clientEnv.locationGranted = true;
                clientEnv.latitude = coords.latitude;
                clientEnv.longitude = coords.longitude;
                clientEnv.accuracy = coords.accuracy;
            } else {
                clientEnv.locationGranted = false;
            }

            startSessionOnBackend(clientEnv);
        })();
    }

    // Call Backend POST /api/session/start
    async function startSessionOnBackend(payload) {
        try {
            const res = await fetch('/api/session/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload || {})
            });

            if (!res.ok) throw new Error('Failed to initialize session');
            const data = await res.json();

            sessionId = data.sessionId;
            selectedChallenge = data.selectedChallenge;

            if (elements.sessionTag) {
                elements.sessionTag.textContent = `SESSION :: ${sessionId.substring(0, 8)}...`;
            }
            elements.btnStart.disabled = false;

            // Transition to CHALLENGE State
            renderChallenge(selectedChallenge);
            switchState(STATES.CHALLENGE);

        } catch (err) {
            console.error('Session start error:', err);
            if (elements.geoStatus) {
                elements.geoStatus.textContent = 'Connection error. Please try again.';
            }
            elements.btnStart.disabled = false;
        }
    }

    // Challenge Rendering Router
    function renderChallenge(challenge) {
        elements.challengeTypeTag.textContent = challenge.type || 'INTERACTIVE';
        elements.challengeTitle.textContent = challenge.title;
        elements.challengeInstructions.textContent = challenge.instructions;
        elements.challengeInteractiveArea.innerHTML = '';

        switch (challenge.id) {
            case 'REACTION_TEST':
                renderReactionTest(challenge.config);
                break;
            case 'MEMORY_SEQUENCE':
                renderMemorySequence(challenge.config);
                break;
            case 'DONT_CLICK':
                renderDontClick(challenge.config);
                break;
            case 'MOVING_BUTTON':
                renderMovingButton(challenge.config);
                break;
            case 'HUMAN_VERIFICATION':
                renderHumanVerification(challenge.config);
                break;
            case 'NUMBER_CHALLENGE':
                renderNumberChallenge(challenge.config);
                break;
            default:
                renderReactionTest({ delayMinMs: 1500, delayMaxMs: 3000 });
        }
    }

    // 1. REACTION TEST
    function renderReactionTest(config) {
        const box = document.createElement('div');
        box.className = 'reaction-target waiting';
        box.textContent = 'WAIT FOR SIGNAL...';
        elements.challengeInteractiveArea.appendChild(box);

        let startTime = 0;
        let ready = false;
        const delay = Math.floor(Math.random() * ((config.delayMaxMs || 3500) - (config.delayMinMs || 1500) + 1)) + (config.delayMinMs || 1500);

        const timer = setTimeout(() => {
            ready = true;
            startTime = Date.now();
            box.className = 'reaction-target ready';
            box.textContent = 'CLICK NOW!';
        }, delay);

        box.addEventListener('click', () => {
            if (!ready) {
                clearTimeout(timer);
                box.textContent = 'TOO EARLY! RESTARTING...';
                setTimeout(() => renderReactionTest(config), 1200);
            } else {
                const reactionTime = Date.now() - startTime;
                box.className = 'reaction-target clicked';
                box.textContent = `REFLEX RECORDED: ${reactionTime} ms!`;
                setTimeout(() => finishChallenge(), 1000);
            }
        });
    }

    // 2. MEMORY SEQUENCE
    function renderMemorySequence(config) {
        const grid = document.createElement('div');
        grid.className = 'memory-pad-grid';
        
        const colors = ['pad-c', 'pad-m', 'pad-y', 'pad-g'];
        const pads = [];

        colors.forEach((c, idx) => {
            const pad = document.createElement('button');
            pad.type = 'button';
            pad.className = `memory-pad-btn ${c}`;
            pad.dataset.index = idx;
            grid.appendChild(pad);
            pads.push(pad);
        });

        elements.challengeInteractiveArea.appendChild(grid);

        const targetSeq = [0, 2, 1, 3];
        let userStep = 0;
        let acceptingInput = false;

        setTimeout(() => playSequence(targetSeq, pads, () => { acceptingInput = true; }), 600);

        pads.forEach(pad => {
            pad.addEventListener('click', () => {
                if (!acceptingInput) return;
                const idx = parseInt(pad.dataset.index);
                flashPad(pad);

                if (idx === targetSeq[userStep]) {
                    userStep++;
                    if (userStep === targetSeq.length) {
                        acceptingInput = false;
                        elements.challengeInstructions.textContent = 'SEQUENCE VERIFIED!';
                        setTimeout(() => finishChallenge(), 1000);
                    }
                } else {
                    acceptingInput = false;
                    elements.challengeInstructions.textContent = 'WRONG PATTERN! RESTARTING...';
                    userStep = 0;
                    setTimeout(() => {
                        elements.challengeInstructions.textContent = config.instructions;
                        playSequence(targetSeq, pads, () => { acceptingInput = true; });
                    }, 1400);
                }
            });
        });
    }

    function playSequence(seq, pads, callback) {
        let i = 0;
        const interval = setInterval(() => {
            if (i >= seq.length) {
                clearInterval(interval);
                if (callback) callback();
                return;
            }
            flashPad(pads[seq[i]]);
            i++;
        }, 500);
    }

    function flashPad(pad) {
        pad.classList.add('active');
        setTimeout(() => pad.classList.remove('active'), 320);
    }

    // 3. DONT CLICK CHALLENGE
    function renderDontClick(config) {
        const wrap = document.createElement('div');
        wrap.style.textAlign = 'center';

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'dont-click-trigger';
        btn.textContent = 'DO NOT CLICK THIS BUTTON';

        const timerDisplay = document.createElement('div');
        timerDisplay.className = 'timer-countdown';
        timerDisplay.textContent = '5.0s';

        wrap.appendChild(btn);
        wrap.appendChild(timerDisplay);
        elements.challengeInteractiveArea.appendChild(wrap);

        let timeLeft = config.countdownSeconds || 5;
        let clickedEarly = false;

        const interval = setInterval(() => {
            if (clickedEarly) return;
            timeLeft -= 0.1;
            if (timeLeft <= 0) {
                clearInterval(interval);
                timerDisplay.textContent = '0.0s - SUCCESS!';
                btn.disabled = true;
                setTimeout(() => finishChallenge(), 1000);
            } else {
                timerDisplay.textContent = `${timeLeft.toFixed(1)}s`;
            }
        }, 100);

        btn.addEventListener('click', () => {
            clickedEarly = true;
            clearInterval(interval);
            timerDisplay.textContent = 'TEMPTATION WON! RESTARTING...';
            setTimeout(() => renderDontClick(config), 1400);
        });
    }

    // 4. MOVING BUTTON
    function renderMovingButton(config) {
        const area = document.createElement('div');
        area.className = 'evasive-container';

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-cyber-primary evasive-target-btn';
        btn.innerHTML = '<span class="btn-text">CLICK TO SUBMIT</span>';
        btn.style.left = '35%';
        btn.style.top = '35%';

        area.appendChild(btn);
        elements.challengeInteractiveArea.appendChild(area);

        let movesRemaining = config.requiredClicks || 4;

        function moveBtn() {
            if (movesRemaining > 1) {
                const maxX = area.clientWidth - 180;
                const maxY = area.clientHeight - 60;
                const newX = Math.max(10, Math.floor(Math.random() * maxX));
                const newY = Math.max(10, Math.floor(Math.random() * maxY));

                btn.style.left = `${newX}px`;
                btn.style.top = `${newY}px`;
                movesRemaining--;
                elements.challengeInstructions.textContent = `Evasive Target! ${movesRemaining} dodges remaining...`;
            } else if (movesRemaining === 1) {
                btn.style.borderColor = 'var(--green-success)';
                btn.style.color = 'var(--green-success)';
                elements.challengeInstructions.textContent = 'TARGET LOCKED! NOW CLICK TO SUBMIT!';
                movesRemaining--;
            }
        }

        btn.addEventListener('mouseover', moveBtn);
        btn.addEventListener('touchstart', moveBtn);
        btn.addEventListener('click', () => {
            if (movesRemaining === 0) {
                btn.innerHTML = '<span class="btn-text">CAPTURED!</span>';
                setTimeout(() => finishChallenge(), 800);
            } else {
                moveBtn();
            }
        });
    }

    // 5. ABSURD HUMAN VERIFICATION
    function renderHumanVerification(config) {
        const wrap = document.createElement('div');
        wrap.className = 'slider-wrapper';

        const slider = document.createElement('input');
        slider.type = 'range';
        slider.min = '0.0';
        slider.max = '100.0';
        slider.step = '0.1';
        slider.value = '14.2';
        slider.className = 'precision-range';

        const display = document.createElement('div');
        display.className = 'precision-readout';
        display.textContent = '14.2%';

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn-cyber-primary';
        btn.style.marginTop = '20px';
        btn.innerHTML = '<span class="btn-text">[ VERIFY CONSCIOUSNESS ]</span>';

        wrap.appendChild(display);
        wrap.appendChild(slider);
        wrap.appendChild(btn);
        elements.challengeInteractiveArea.appendChild(wrap);

        const target = config.targetValue || 42.7;

        slider.addEventListener('input', () => {
            display.textContent = `${parseFloat(slider.value).toFixed(1)}%`;
        });

        btn.addEventListener('click', () => {
            const val = parseFloat(slider.value);
            if (Math.abs(val - target) <= (config.tolerance || 1.5)) {
                display.style.color = 'var(--green-success)';
                display.textContent = `${val.toFixed(1)}% - VERIFIED!`;
                setTimeout(() => finishChallenge(), 1000);
            } else {
                display.style.color = 'var(--red-alert)';
                elements.challengeInstructions.textContent = `Calibrate closer to target (${target}%)!`;
                setTimeout(() => { display.style.color = 'var(--cyan-accent)'; }, 1200);
            }
        });
    }

    // 6. NUMBER CHALLENGE
    function renderNumberChallenge(config) {
        const flex = document.createElement('div');
        flex.className = 'sequence-flex';

        const numbers = [1, 2, 3, 4, 5];
        numbers.sort(() => Math.random() - 0.5);

        let nextExpected = 1;

        numbers.forEach(num => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'sequence-num-btn';
            btn.textContent = num;
            btn.dataset.num = num;

            btn.addEventListener('click', () => {
                const clicked = parseInt(btn.dataset.num);
                if (clicked === nextExpected) {
                    btn.classList.add('correct');
                    nextExpected++;
                    if (nextExpected > 5) {
                        elements.challengeInstructions.textContent = 'SEQUENCE VERIFIED!';
                        setTimeout(() => finishChallenge(), 800);
                    }
                } else {
                    elements.challengeInstructions.textContent = 'WRONG ORDER! RESETTING...';
                    setTimeout(() => renderNumberChallenge(config), 1000);
                }
            });

            flex.appendChild(btn);
        });

        elements.challengeInteractiveArea.appendChild(flex);
    }

    // Challenge Completion Signal
    async function finishChallenge() {
        try {
            await fetch('/api/challenge/complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sessionId: sessionId })
            });

            runAnalysisSequence();
        } catch (err) {
            console.error('Error completing challenge:', err);
            runAnalysisSequence();
        }
    }

    // Staged Analysis Sequence
    function runAnalysisSequence() {
        switchState(STATES.ANALYSIS);
        elements.analysisFeed.innerHTML = '';
        if (elements.analysisProgress) elements.analysisProgress.style.width = '0%';
        if (elements.analysisPercent) elements.analysisPercent.textContent = '0%';

        const steps = [
            'ANALYZING SESSION...',
            'VERIFYING CONNECTION...',
            'READING REQUEST...',
            'IDENTIFYING CONNECTION...',
            'ANALYSIS COMPLETE'
        ];

        let index = 0;
        const totalSteps = steps.length;

        const interval = setInterval(async () => {
            if (index < totalSteps) {
                const line = document.createElement('div');
                line.className = 'analysis-line';
                line.textContent = `> ${steps[index]}`;
                elements.analysisFeed.appendChild(line);

                const percent = Math.round(((index + 1) / totalSteps) * 100);
                if (elements.analysisProgress) {
                    elements.analysisProgress.style.width = `${percent}%`;
                }
                if (elements.analysisPercent) {
                    elements.analysisPercent.textContent = `${percent}%`;
                }

                index++;
            } else {
                clearInterval(interval);
                await fetchAndDisplayRevealState();
            }
        }, 700);
    }

    // Staged Reveal Sequence -> Transitions to PRANK
    async function fetchAndDisplayRevealState() {
        try {
            const res = await fetch(`/api/session/state?sessionId=${encodeURIComponent(sessionId)}`);
            if (!res.ok) throw new Error('State fetch failed');
            const data = await res.json();

            // Populate reveal data for the Educational Reveal Screen
            if (elements.revealIp) {
                elements.revealIp.textContent = data.connectionIp || '127.0.0.1';
            }

            let locText = '';
            if (data.locationGranted && data.latitude != null && data.longitude != null) {
                locText = `${data.latitude.toFixed(4)}°, ${data.longitude.toFixed(4)}° (Browser GPS) | ${data.ipCity || 'Localhost'}, ${data.ipCountry || ''}`;
            } else if (data.ipCity || data.ipCountry) {
                locText = `${data.ipCity || 'Localhost'}, ${data.ipRegion || ''} ${data.ipCountry || ''} (ISP: ${data.isp || 'Local Network'})`;
            } else {
                locText = `Localhost Development Gateway`;
            }

            if (elements.revealLocation) {
                elements.revealLocation.textContent = locText;
            }
            if (elements.revealStatus) {
                elements.revealStatus.textContent = 'ANALYSIS COMPLETE';
            }

            // Dramatic Pause before PRANK state transition
            setTimeout(() => {
                switchState(STATES.PRANK);
            }, 1200);

        } catch (err) {
            console.error('Error fetching reveal state:', err);
            if (elements.revealIp) elements.revealIp.textContent = '127.0.0.1';
            if (elements.revealLocation) elements.revealLocation.textContent = 'Location resolved.';
            setTimeout(() => switchState(STATES.PRANK), 1200);
        }
    }

    // Boot App on DOM Load
    document.addEventListener('DOMContentLoaded', init);

})();
