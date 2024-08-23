package io.cax.pdf_ext.controller;
import io.cax.pdf_ext.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("/session")
public class SessionController {

public static final String XSESSION = "X__SESSION__X";

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Create a new session
     * @return
     */
    @PostMapping("/start")
    public ResponseEntity<String> createSession() {
        UUID sessionId = sessionService.createSession();
        return ResponseEntity.ok(sessionId.toString());
    }

    /**
     * End session
     * @param headerValue
     * @return
     */
    @PostMapping("/end")
    public ResponseEntity<Object> endSearchSession(@RequestHeader(XSESSION) String headerValue) {
        //HEADER cannot be null or empty
        if (headerValue == null || headerValue.isEmpty()) {
            return ResponseEntity.badRequest().body("Session header is required");
        }
        sessionService.endSession(UUID.fromString(headerValue));
        return ResponseEntity.ok().build();
    }
}
