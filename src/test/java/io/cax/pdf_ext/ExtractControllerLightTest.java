package io.cax.pdf_ext;

import io.cax.pdf_ext.controller.ExtractController;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.micrometer.core.instrument.MeterRegistry;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public class ExtractControllerLightTest {

    @Mock
    private ExtractorEngine extractorEngine;

    @InjectMocks
    private ExtractController extractController;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(extractController, "tempFolder", "/tmp/");

    }

    @Test
    void uploadFileReturnsOkWhenFileIsNotEmpty() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(extractorEngine, times(1)).extractTextFrom(anyString());
    }

    @Test
    void uploadFileReturnsOkWhenFileIsEmpty() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "".getBytes());

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Please select a file!", response.getBody());
        verify(extractorEngine, never()).extractTextFrom(anyString());
    }

    @Test
    void uploadFileReturnsExpectationFailedWhenExceptionOccurs() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenThrow(IOException.class);

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
        assertEquals("Could not upload the file: test.pdf!", response.getBody());
        verify(extractorEngine, times(1)).extractTextFrom(anyString());
    }


    @Test
    void uploadFileGeneratesUniqueFileName() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        extractController.uploadFile(file);

        verify(extractorEngine, times(1)).extractTextFrom(matches(".+\\.pdf$"));
    }

    @Test
    void uploadFileSavesFileToTempFolder() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenReturn(new JSONObject());

        extractController.uploadFile(file);

        verify(extractorEngine, times(1)).extractTextFrom(matches("^" + ReflectionTestUtils.getField(extractController, "tempFolder") + ".+\\.pdf$"));
    }

    @Test
    void uploadFileReturnsErrorWhenExtractionFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenThrow(new RuntimeException("Extraction failed"));

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while processing the file: test.pdf!", response.getBody());
    }
}
