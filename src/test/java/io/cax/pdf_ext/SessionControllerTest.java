package io.cax.pdf_ext;

import io.cax.pdf_ext.controller.SessionController;
import io.cax.pdf_ext.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SessionControllerTest is a test class for the SessionController.
 */
public class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    private SessionController sessionController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        sessionController = new SessionController(sessionService);
    }

    /**
     * Test the createSession method.
     */
    @Test
    public void testCreateSession() {
        UUID mockSessionId = UUID.randomUUID();
        when(sessionService.createSession()).thenReturn(mockSessionId);

        ResponseEntity<String> response = sessionController.createSession();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockSessionId, response.getBody());
        verify(sessionService, times(1)).createSession();
    }


}