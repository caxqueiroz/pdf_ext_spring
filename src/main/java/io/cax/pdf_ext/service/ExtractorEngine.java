package io.cax.pdf_ext.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class ExtractorEngine {

    private final Logger logger = Logger.getLogger(ExtractorEngine.class.getName());
    private final Timer extractTextFromTimer;
    private final Counter successfulExtractsCounter;


    /**
     * Create a new ExtractorEngine.
     * @param meterRegistry The meter registry to register metrics with.
     */
    public ExtractorEngine(MeterRegistry meterRegistry) {
        this.extractTextFromTimer = Timer
                .builder("ExtractText")
                .description("Time taken to extract text from PDF")
                .register(meterRegistry);

        this.successfulExtractsCounter = Counter
                .builder("successfulExtracts")
                .description("Number of successful text extracts from PDF")
                .register(meterRegistry);
    }

    /**
     * Extract text from a PDF file. The extracted text is returned as a JSON object.
     * @param inputFile The path to the PDF file to extract text from.
     * @return A JSON object containing the extracted text.
     * @throws IOException If an error occurs while reading the document.
     */
    public JSONObject extractTextFrom(String inputFile) throws IOException {
        return extractTextFromTimer.record(() -> {
            try {
                JSONObject result  = doExtractTextFrom(inputFile);
                successfulExtractsCounter.increment();
                return result;
            } catch(Exception e) {
                logger.severe(e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Extract text from a PDF file. The extracted text is returned as a JSON object.
     * The JSON object contains the following fields:
     * - doc_title: The title of the document.
     * - filename: The name of the file.
     *
     * @param inputFile The path to the PDF file to extract text from.
     * @return A JSON object containing the extracted text.
     * @throws IOException If an error occurs while reading the document.
     */
    private JSONObject doExtractTextFrom(String inputFile) throws IOException {
        JSONObject doc = new JSONObject();

        File f = new File(inputFile);
        String fileName = f.getName();
        PDDocument pdDocument = Loader.loadPDF(f);
        doc.put("doc_title", getTitle(pdDocument));
        doc.put("filename", fileName);

        PDFTextStripper pdfStripper = new PDFTextStripper();
        int nPages = pdDocument.getNumberOfPages();
        doc.put("total_pages", String.valueOf(nPages));
        JSONArray pages = new JSONArray();


        for (int i = 1; i <= nPages; i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String pageText = pdfStripper.getText(pdDocument);
            JSONObject page = new JSONObject();
            page.put("page_number", String.valueOf(i));
            page.put("page_text", pageText);
            pages.put(page);
        }
        doc.put("pages", pages);

        return doc;
    }

    /**
     * Get the title of a PDF document.
     * If the document has a title, it is returned. Otherwise, the title of the first page is returned.
     * @param pdDocument The PDF document to get the title from.
     * @return The title of the PDF document.
     * @throws IOException If an error occurs while reading the document.
     */
    private String getTitle(PDDocument pdDocument) throws IOException {
        String title = pdDocument.getDocumentInformation().getTitle();
        if (title != null && !title.isEmpty()) {
            return title.replace("\n", " ");
        }
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        pdfTextStripper.setStartPage(1);
        pdfTextStripper.setEndPage(1);
        String pageText = pdfTextStripper.getText(pdDocument);
        return pageText.replace("\n", " ");
    }
}
