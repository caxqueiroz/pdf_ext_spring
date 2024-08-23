package io.cax.pdf_ext.service;

import io.cax.pdf_ext.model.Session;
import io.cax.pdf_ext.model.XDoc;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final ConcurrentHashMap<UUID, Session> sessionRepository;

    public SessionService() {
        this.sessionRepository = new ConcurrentHashMap<>();
    }

    //get content of session
    public Session getSession(UUID sessionId) {
        return sessionRepository.get(sessionId);
    }

    //check if session exists
    public boolean sessionExists(UUID sessionId) {
        return sessionRepository.containsKey(sessionId);
    }

    /**
     * Create a new session
     * @return
     */
    public UUID createSession() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setSessionId(sessionId);
        sessionRepository.put(sessionId, session);
        return sessionId;
    }

    /**
     * End session
     * @param uuid
     */
    public void endSession(UUID uuid) {
        sessionRepository.remove(uuid);
    }
}