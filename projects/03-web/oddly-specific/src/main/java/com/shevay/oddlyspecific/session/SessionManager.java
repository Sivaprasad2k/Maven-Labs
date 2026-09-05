package com.shevay.oddlyspecific.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<String, Session> expiredSessions = new ConcurrentHashMap<>();
    private final long sessionTtlMs;

    public SessionManager() {
        this(3600 * 1000L); // Default 1 hour TTL
    }

    public SessionManager(long sessionTtlMs) {
        this.sessionTtlMs = sessionTtlMs;
    }

    public Session createSession(String selectedChallengeId, String connectionIp) {
        cleanupExpiredSessions();
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, selectedChallengeId, connectionIp);
        sessions.put(sessionId, session);
        return session;
    }

    public Optional<Session> getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        cleanupExpiredSessions();
        Session s = sessions.get(sessionId);
        if (s != null) return Optional.of(s);
        return Optional.ofNullable(expiredSessions.get(sessionId));
    }

    public boolean updateLocation(String sessionId, boolean locationGranted, Double latitude, Double longitude, Double accuracy) {
        Optional<Session> opt = getSession(sessionId);
        if (opt.isPresent()) {
            Session s = opt.get();
            s.setLocationGranted(locationGranted);
            s.setLatitude(latitude);
            s.setLongitude(longitude);
            s.setAccuracy(accuracy);
            return true;
        }
        return false;
    }

    public boolean updateIpLocation(String sessionId, String city, String region, String country, Double ipLat, Double ipLng, String isp) {
        Optional<Session> opt = getSession(sessionId);
        if (opt.isPresent()) {
            Session s = opt.get();
            s.setIpCity(city);
            s.setIpRegion(region);
            s.setIpCountry(country);
            s.setIpLatitude(ipLat);
            s.setIpLongitude(ipLng);
            s.setIsp(isp);
            return true;
        }
        return false;
    }

    public boolean updateClientEnvironment(String sessionId, String userAgent, String platform, String language, String timezone, String screenResolution) {
        Optional<Session> opt = getSession(sessionId);
        if (opt.isPresent()) {
            Session s = opt.get();
            s.setUserAgent(userAgent);
            s.setPlatform(platform);
            s.setLanguage(language);
            s.setTimezone(timezone);
            s.setScreenResolution(screenResolution);
            return true;
        }
        return false;
    }

    public boolean completeSession(String sessionId) {
        Optional<Session> opt = getSession(sessionId);
        if (opt.isPresent()) {
            opt.get().setCompleted(true);
            return true;
        }
        return false;
    }

    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getCreatedAt()) > sessionTtlMs;
            if (expired) {
                expiredSessions.put(entry.getKey(), entry.getValue());
            }
            return expired;
        });
    }

    public Collection<Session> getActiveSessions() {
        cleanupExpiredSessions();
        return new ArrayList<>(sessions.values());
    }

    public Collection<Session> getExpiredSessions() {
        return new ArrayList<>(expiredSessions.values());
    }

    public boolean deleteSession(String sessionId) {
        if (sessionId == null) return false;
        Session removedActive = sessions.remove(sessionId);
        Session removedExpired = expiredSessions.remove(sessionId);
        return (removedActive != null || removedExpired != null);
    }

    public boolean expireSession(String sessionId) {
        Session s = sessions.remove(sessionId);
        if (s != null) {
            expiredSessions.put(sessionId, s);
            return true;
        }
        return false;
    }

    public int getActiveSessionCount() {
        cleanupExpiredSessions();
        return sessions.size();
    }

    public void clearAll() {
        sessions.clear();
        expiredSessions.clear();
    }
}
