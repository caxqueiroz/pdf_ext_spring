package one.cax.doc_search;

import one.cax.doc_search.controller.ExtractController;
import one.cax.doc_search.exception.DocumentExtractionException;
import one.cax.doc_search.exception.FileServiceException;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.service.ExtractorEngine;
import one.cax.doc_search.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExtractControllerLightTest {

    @Mock
    private ExtractorEngine extractorEngine;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ExtractController extractController;


    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(extractController, "tempFolder", "/tmp/");

    }

    @Test
    void uploadFileReturnsOkWhenFileIsNotEmpty() throws FileServiceException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fileService, times(1)).extractTextFrom(anyString());
    }

    @Test
    void uploadFileReturnsOkWhenFileIsEmpty() throws FileServiceException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "".getBytes());

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Please upload a file!", response.getBody());
        verify(fileService, never()).extractTextFrom(anyString());
    }

    @Test
    void uploadFileReturnsExpectationFailedWhenExceptionOccurs() throws FileServiceException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(fileService.extractTextFrom(anyString())).thenThrow(FileServiceException.class);

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
        assertEquals("Could not upload the file: test.pdf!", response.getBody());
        verify(fileService, times(1)).extractTextFrom(anyString());
    }


    @Test
    void uploadFileGeneratesUniqueFileName() throws FileServiceException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

        extractController.uploadFile(file);

        verify(fileService, times(1)).extractTextFrom(matches(".+\\.pdf$"));
    }

    @Test
    void uploadFileSavesFileToTempFolder() throws FileServiceException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(fileService.extractTextFrom(anyString())).thenReturn(new XDoc());

        extractController.uploadFile(file);

        verify(fileService, times(1)).extractTextFrom(matches("^" + ReflectionTestUtils.getField(extractController, "tempFolder") + ".+\\.pdf$"));
    }

    @Test
    void uploadFileReturnsErrorWhenExtractionFails() throws DocumentExtractionException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        when(extractorEngine.extractTextFrom(anyString())).thenThrow(new RuntimeException("Extraction failed"));

        ResponseEntity<Object> response = extractController.uploadFile(file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while processing the file: test.pdf!", response.getBody());
    }
}
