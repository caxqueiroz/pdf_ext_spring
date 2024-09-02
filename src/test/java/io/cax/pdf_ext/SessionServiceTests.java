package io.cax.pdf_ext;

import io.cax.pdf_ext.service.SessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionServiceTests {

    private SessionService sessionService;

    @BeforeEach
    public void setup() {
        sessionService = new SessionService(); // Assuming a no-arg constructor
    }

    @Test
    public void testCreateSession() {
        UUID sessionId = sessionService.createSession();
        assertNotNull(sessionId);
    }

     @Test
    void testSessionExists() {
        // First, create a session
        UUID sessionId = sessionService.createSession();

        // Then check if it exists
        boolean exists = sessionService.sessionExists(sessionId);
        assertTrue(exists);

        // Check for a non-existent session
        UUID nonExistentId = UUID.randomUUID();
        boolean nonExistentExists = sessionService.sessionExists(nonExistentId);
        assertFalse(nonExistentExists);
    }

    @Test
    void testEndSession() {
        UUID sessionId = sessionService.createSession();
        sessionService.endSession(sessionId);
        assertFalse(sessionService.sessionExists(sessionId));
    }
}
