package io.cax.pdf_ext.controller;

import io.cax.pdf_ext.exception.VectorSearchException;
import io.cax.pdf_ext.model.XDoc;
import io.cax.pdf_ext.service.ExtractorEngine;
import io.cax.pdf_ext.service.VectorSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping("/search")
@Tag(name = "VectorController", description = "Controller for vector search operations")
public class VectorController {

    @Value("${doc_ext_search.temp_folder}")
    private String tempFolder;

    private final ExtractorEngine extractorEngine;

    @Autowired
    public VectorController(ExtractorEngine extractorEngine) {
        this.extractorEngine = extractorEngine;
    }

    private VectorSearch vectorService;

    @Autowired
    public void setVectorService(VectorSearch vectorService) {
        this.vectorService = vectorService;
    }

    /**
     * Add a document to the vector space.
     * @param document - document to add
     * @param sessionId - session id
     * @return
     */
    @PostMapping("/{sessionId}/doc")
    @Operation(summary = "Add a document", description = "Adds a document to the vector space")
    public ResponseEntity<Object> addDocument(@RequestBody String document, @PathVariable("sessionId") String sessionId) {
        try {
            var xDoc = XDoc.fromText(document);
            var docId = vectorService.addDocument(UUID.fromString(sessionId), xDoc);
            return new ResponseEntity<>(docId.toString(), HttpStatus.OK);
        } catch (VectorSearchException ve) {
            return new ResponseEntity<>("An error occurred while adding the document: " + ve.getMessage() + "!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while adding the document: " + document + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload a document to the vector space.
     * @param file - file to upload
     * @param sessionId - session id    
     * @return - response entity
     */
    @PostMapping("/{sessionId}/upload")
    @Operation(summary = "Upload a document", description = "Uploads a document to the vector space")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file, @PathVariable("sessionId") String sessionId) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please select a file!", HttpStatus.OK);
            }

            
            var xDoc = extractorEngine.extractTextFromPDF(file.getBytes());
            var docId = vectorService.addDocument(UUID.fromString(sessionId), xDoc);
            return new ResponseEntity<>(docId.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while uploading the document: " + e.getMessage() + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search for a query in the vector space.
     * @param query - query to search
     * @return - response entity
     */
    @PostMapping("/{sessionId}/query")
    @Operation(summary = "Search for a query", description = "Searches for a query in the vector space")
    public ResponseEntity<Object> search(@RequestBody String query, @PathVariable("sessionId") String sessionId) {
        try {
            JSONObject response = vectorService.search(UUID.fromString(sessionId), query);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (VectorSearchException ve) {
            return new ResponseEntity<>("An error occurred while processing the query: " + ve.getMessage() + "!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while processing the query: " + query + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
