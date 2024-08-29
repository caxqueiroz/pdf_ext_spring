package io.cax.pdf_ext.controller;

import io.cax.pdf_ext.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/session")
@Tag(name = "SessionController", description = "Controller for managing search sessions")
public class SessionController {

public static final String XSESSION = "X__SESSION__X";

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Create a new session
     * @return - response entity
     */
    @PostMapping("/start")
    @Operation(summary = "Create a new session", description = "Creates a new session")
    public ResponseEntity<String> createSession() {
        UUID sessionId = sessionService.createSession();
        return ResponseEntity.ok(sessionId.toString());
    }

    /**
     * Ends session. all session data will be deleted
     * @param headerValue - session header
     * @return - response entity
     */
    @PostMapping("/end")
    @Operation(summary = "End a session", description = "Ends a session and deletes all session data")
    public ResponseEntity<Object> endSearchSession(@RequestHeader(XSESSION) String headerValue) {
        //HEADER cannot be null or empty
        if (headerValue == null || headerValue.isEmpty()) {
            return ResponseEntity.badRequest().body("Session header is required");
        }
        sessionService.endSession(UUID.fromString(headerValue));
        return ResponseEntity.ok().build();
    }
}