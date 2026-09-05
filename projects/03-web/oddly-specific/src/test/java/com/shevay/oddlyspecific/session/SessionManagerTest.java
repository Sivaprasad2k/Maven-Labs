package com.shevay.oddlyspecific.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(5000); // 5 sec TTL for testing
    }

    @Test
    void testCreateSession() {
        Session session = sessionManager.createSession("REACTION_TEST", "192.168.1.100");
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals("REACTION_TEST", session.getSelectedChallengeId());
        assertEquals("192.168.1.100", session.getConnectionIp());
        assertFalse(session.isLocationGranted());
        assertFalse(session.isCompleted());
    }

    @Test
    void testGetSession() {
        Session session = sessionManager.createSession("DONT_CLICK", "10.0.0.1");
        Optional<Session> retrieved = sessionManager.getSession(session.getSessionId());

        assertTrue(retrieved.isPresent());
        assertEquals("DONT_CLICK", retrieved.get().getSelectedChallengeId());
    }

    @Test
    void testGetNonExistentSession() {
        Optional<Session> retrieved = sessionManager.getSession("invalid-session-id");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void testUpdateLocation() {
        Session session = sessionManager.createSession("MOVING_BUTTON", "172.16.0.1");
        boolean updated = sessionManager.updateLocation(session.getSessionId(), true, 37.7749, -122.4194, 10.0);

        assertTrue(updated);
        Session s = sessionManager.getSession(session.getSessionId()).orElseThrow();
        assertTrue(s.isLocationGranted());
        assertEquals(37.7749, s.getLatitude());
        assertEquals(-122.4194, s.getLongitude());
        assertEquals(10.0, s.getAccuracy());
    }

    @Test
    void testCompleteSession() {
        Session session = sessionManager.createSession("HUMAN_VERIFICATION", "192.168.1.5");
        boolean completed = sessionManager.completeSession(session.getSessionId());

        assertTrue(completed);
        Session s = sessionManager.getSession(session.getSessionId()).orElseThrow();
        assertTrue(s.isCompleted());
    }
}
