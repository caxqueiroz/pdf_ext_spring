package one.cax.doc_search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.service.ExtractorEngine;
import one.cax.doc_search.service.VectorSearch;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * VectorController is responsible for handling vector search operations.
 * 
 * This controller provides endpoints for adding documents to the vector space,
 * uploading files, and performing vector-based searches. It integrates with
 * ExtractorEngine for text extraction and VectorSearch for search operations.
 * 
 * Key functionalities:
 * - Adding documents to the vector space
 * - Uploading files and processing them for vector search
 * - Performing vector-based searches on added documents
 * 
 * The controller uses SessionService (indirectly through VectorSearch) to manage
 * user sessions and ensure proper isolation of search contexts.
 */
@RestController
@RequestMapping("/search")
@Tag(name = "VectorController", description = "Controller for vector search operations")
public class VectorController {

    private final ExtractorEngine extractorEngine;
    @Value("${doc_ext_search.temp_folder}")
    private String tempFolder;
    private VectorSearch vectorService;

    @Autowired
    public VectorController(ExtractorEngine extractorEngine) {
        this.extractorEngine = extractorEngine;
    }

    @Autowired
    public void setVectorService(VectorSearch vectorService) {
        this.vectorService = vectorService;
    }

    /**
     * Add a document (as text) to the vector space.
     *
     * @param document  - document to add
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
        } catch (VectorSearchException | JSONException e) {
            return new ResponseEntity<>("An error occurred while adding the document: " + e.getMessage() + "!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while adding the document: " + document + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload a document to the vector space.
     *
     * @param file      - file to upload
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
     *
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
