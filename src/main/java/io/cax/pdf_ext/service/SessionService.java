package io.cax.pdf_ext.service;

import io.cax.pdf_ext.model.Session;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionService is a service that manages user sessions.
 */
@Service
public class SessionService {

    /* The session repository - stores all the content per user session */
    private final ConcurrentHashMap<UUID, Session> sessionRepository;

    /**
     * Create a new SessionService.
     */
    public SessionService() {
        this.sessionRepository = new ConcurrentHashMap<>();
    }

    /**
     * Get the session
     * @param sessionId - the session id
     * @return - the session as UUID
     */
    public Session getSession(UUID sessionId) {
        return sessionRepository.get(sessionId);
    }

    /**
     * Check if the session exists
     * @param sessionId - the session id
     * @return - true if the session exists, false otherwise
     */
    public boolean sessionExists(UUID sessionId) {
        return sessionRepository.containsKey(sessionId);
    }

    /**
     * Create a new session
     * @return - the session id
     */
    public UUID createSession() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setSessionId(sessionId);
        sessionRepository.put(sessionId, session);
        return sessionId;
    }

    /**
     * Ends the current user session. Removes the session from the repository.
     * @param uuid - the session id
     */
    public void endSession(UUID uuid) {
        sessionRepository.remove(uuid);
    }
}