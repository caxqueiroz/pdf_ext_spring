package io.cax.pdf_ext;

import io.cax.pdf_ext.exception.DocumentExtractionException;
import io.cax.pdf_ext.model.XDoc;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;

public class ExtractionEngineTests {

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    public void testExtractTextFromPDF_NotNull() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("test data");
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getMetadata());
        assertNotNull(xDoc.getDocTitle());
        assertNotNull(xDoc.getId());

    }

    @Test
    public void testExtractTextFromPDF_Null() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = null;
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }

    @Test
    public void testExtractTextFromPDF_Empty() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("");
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getMetadata());
        
    }

    @Test
    public void testExtractTextFromPDF_Large() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = TestUtils.createPdf("a".repeat(1000000));
        XDoc xDoc = extractorEngine.extractTextFromPDF(fileInBytes);
        assertNotNull(xDoc.getMetadata());
      
    }

    @Test
    public void testExtractTextFromPDF_Invalid() throws DocumentExtractionException {
        ExtractorEngine extractorEngine = new ExtractorEngine(meterRegistry);
        byte[] fileInBytes = "This is not a valid PDF".getBytes();
        assertThrows(DocumentExtractionException.class, () -> extractorEngine.extractTextFromPDF(fileInBytes));
    }   

}
