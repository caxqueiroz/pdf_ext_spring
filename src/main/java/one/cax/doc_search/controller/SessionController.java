package one.cax.doc_search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import one.cax.doc_search.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * SessionController is responsible for managing user sessions in the document search system.
 * 
 * This controller provides endpoints for creating new sessions, checking the existence of sessions,
 * and ending sessions. It interacts with the SessionService to perform these operations.
 * 
 * Key functionalities:
 * - Creating new sessions with unique UUIDs
 * - Checking if a session exists by its ID
 * - Ending (removing) sessions and their associated data
 * 
 * The controller uses RESTful principles and returns appropriate HTTP status codes and
 * response bodies for each operation. It also utilizes Swagger annotations for API documentation.
 */
@RestController
@RequestMapping("/session")
@Tag(name = "SessionController", description = "Controller for managing search sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Create a new session
     *
     * @return - response entity
     */
    @PostMapping("/start")
    @Operation(summary = "Create a new session", description = "Creates a new session")
    public ResponseEntity<String> createSession() {
        UUID sessionId = sessionService.createSession();
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/session/{id}")
                .buildAndExpand(sessionId)
                .toUri();
        return ResponseEntity.created(location).body(sessionId.toString());
    }

    /**
     * Checks session id
     *
     * @param sessionId - session id
     * @return - response entity
     */
    @GetMapping("/{id}")
    @Operation(summary = "Checks session id", description = "Checks if the session id exists")
    public ResponseEntity<String> checkSessionId(@PathVariable("id") String sessionId) {
        if (sessionService.sessionExists(UUID.fromString(sessionId))) {
            return ResponseEntity.ok(sessionId);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Ends session. all session data will be deleted
     *
     * @param sessionId - session id
     * @return - response entity
     */
    @PutMapping("/end/{id}")
    @Operation(summary = "End a session", description = "Ends a session and deletes all session data")
    public ResponseEntity<Object> endSearchSession(@PathVariable("id") String sessionId) {
        //path variable cannot be null or empty
        if (sessionId == null || sessionId.isEmpty()) {
            return ResponseEntity.badRequest().body("Session header is required");
        }
        sessionService.endSession(UUID.fromString(sessionId));
        return ResponseEntity.ok().build();
    }
}