package one.cax.doc_search.service;

import one.cax.doc_search.exception.DocumentExtractionException;
import one.cax.doc_search.model.NameUtils;
import one.cax.doc_search.model.XDoc;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * ExtractorEngine is a service that extracts text from a PDF file.
 * The extracted text is returned as a JSON object.
 */
@Service
public class ExtractorEngine {

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
     * @throws DocumentExtractionException If an error occurs while reading the document.
     */
    public JSONObject extractTextFrom(String inputFile) throws DocumentExtractionException {
        try {
            return extractTextFromTimer.recordCallable(() -> {
                JSONObject result = doExtractTextFrom(inputFile);
                successfulExtractsCounter.increment();
                return result;
            });
        } catch (Exception e) {
                throw new DocumentExtractionException("Error extracting text from PDF", e);
        }
    }
    /**
     * Extract text from a PDF file. The extracted text is returned as a JSON object.
     * @param fileInBytes The PDF byte array to extract text from.
     * @return a XDoc object containing the extracted text.
     * @throws DocumentExtractionException If an error occurs while processing the document.
     */
    public XDoc extractTextFromPDF(byte[] fileInBytes) throws DocumentExtractionException {

        //check for null 
        if (fileInBytes == null) {
            throw new DocumentExtractionException("File is null");
        }
        Timer.Sample sample = Timer.start();
        try {
            JSONObject doc = new JSONObject();
            XDoc xDoc = new XDoc();
            try (PDDocument pdDocument = Loader.loadPDF(fileInBytes)) {
                xDoc.setDocTitle(getTitle(pdDocument));
                xDoc.setFilename("file.pdf"); 
                xDoc.setTotalPages(pdDocument.getNumberOfPages());
                
                
                JSONArray pages = new JSONArray();
                PDFTextStripper pdfStripper = new PDFTextStripper();
                for (int i = 1; i <= pdDocument.getNumberOfPages(); i++) {
                    pdfStripper.setStartPage(i);
                    pdfStripper.setEndPage(i);
                    String pageText = pdfStripper.getText(pdDocument);
                    pages.put(new JSONObject()
                        .put(NameUtils.PAGE_NUMBER, i)
                        .put(NameUtils.PAGE_TEXT, pageText));
                }
                doc.put(NameUtils.DOC_PAGES, pages);

                successfulExtractsCounter.increment();
                return xDoc;
            } catch (IOException e) {
                throw new DocumentExtractionException("Error extracting text from PDF", e);
            }
        } finally {
            sample.stop(extractTextFromTimer);
        }
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
    private JSONObject doExtractTextFrom(String inputFile) throws IOException, JSONException {
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
            return title.replace("\n", "");
        }
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        pdfTextStripper.setStartPage(1);
        pdfTextStripper.setEndPage(1);
        String pageText = pdfTextStripper.getText(pdDocument);
        return pageText.replace("\n", " ");
    }

    /**
     * Convert a JSON object to a HashMap.
     * @param doc The JSON object to convert.
     * @return A HashMap containing the metadata.
     */
    private HashMap<String, Object> convertToMetadata(JSONObject doc) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put(NameUtils.DOC_TOTAL_PAGES, doc.get(NameUtils.DOC_TOTAL_PAGES));
        metadata.put(NameUtils.DOC_FILENAME, doc.get(NameUtils.DOC_FILENAME));
        metadata.put(NameUtils.DOC_TITLE, doc.get(NameUtils.DOC_TITLE));
        return metadata;
    }
}