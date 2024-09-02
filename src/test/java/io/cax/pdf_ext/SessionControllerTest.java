package io.cax.pdf_ext;

import io.cax.pdf_ext.controller.SessionController;
import io.cax.pdf_ext.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCreateSession() throws Exception {
        UUID mockSessionId = UUID.randomUUID();
        when(sessionService.createSession()).thenReturn(mockSessionId);

        mockMvc.perform(post("/session/start"))
                .andExpect(status().isCreated())
                .andExpect(content().string(mockSessionId.toString()));

        verify(sessionService, times(1)).createSession();
    }

    @Test
    void testCheckSessionId() throws Exception {
        UUID mockSessionId = UUID.randomUUID();
        when(sessionService.sessionExists(mockSessionId)).thenReturn(true);

        mockMvc.perform(get("/session/{id}", mockSessionId))
                .andExpect(status().isOk())
                .andExpect(content().string(mockSessionId.toString()));

        verify(sessionService, times(1)).sessionExists(mockSessionId);
    }

    @Test
    void testCheckSessionIdNotFound() throws Exception {
        UUID mockSessionId = UUID.randomUUID();
        when(sessionService.sessionExists(mockSessionId)).thenReturn(false);

        mockMvc.perform(get("/session/{id}", mockSessionId))
                .andExpect(status().isNotFound());

        verify(sessionService, times(1)).sessionExists(mockSessionId);
    }

    @Test
    void testEndSession() throws Exception {
        UUID mockSessionId = UUID.randomUUID();
        when(sessionService.sessionExists(mockSessionId)).thenReturn(true);

        mockMvc.perform(put("/session/end/{id}", mockSessionId))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).endSession(mockSessionId);
    }
}