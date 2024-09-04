package one.cax.doc_search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.AddTextRequest;
import one.cax.doc_search.model.AddTextResponse;
import one.cax.doc_search.model.SearchRequest;
import one.cax.doc_search.model.SearchResponse;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.service.ExtractorEngine;
import one.cax.doc_search.service.VectorSearch;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
     * the doocument must be in JSON format. 
     * 
     * @param dataRequest  - document to add
     * @param sessionId - session id
     * @return
     */
    @PostMapping(value = "/{sessionId}/addText", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adds text to the session", description = "Adds a document to the vector space (session)")
    public ResponseEntity<AddTextResponse> addDocument(@Schema(implementation = AddTextRequest.class) @RequestBody AddTextRequest dataRequest, @PathVariable("sessionId") String sessionId) {
        
        try {

            var xDoc = XDoc.fromText(dataRequest.getContent());
            var docId = vectorService.addDocument(UUID.fromString(sessionId), xDoc);
            var response = new AddTextResponse();
            response.setSessionId(sessionId);
            response.setTextId(docId.toString());
            response.setResponseText("text added successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (VectorSearchException | JSONException e) {
            var response = new AddTextResponse();
            response.setSessionId(sessionId);
            response.setResponseText("An error occurred while adding the document: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            var response = new AddTextResponse();
            response.setSessionId(sessionId);
            response.setResponseText("An internal server error occurred while adding the document.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Upload a document to the vector space.
     *
     * @param file      - file to upload
     * @param sessionId - session id
     * @return - response entity
     */
    @PostMapping(value = "/{sessionId}/addFile", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload a document", description = "Uploads a document to the vector space")
    public ResponseEntity<AddTextResponse> uploadDocument(@RequestParam("file") MultipartFile file, @PathVariable("sessionId") String sessionId) {

        try {
            if (file.isEmpty()) {
                var response = new AddTextResponse();
                response.setSessionId(sessionId);
                response.setResponseText("Please select a file!");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            var xDoc = extractorEngine.extractTextFromPDF(file.getBytes());
            var docId = vectorService.addDocument(UUID.fromString(sessionId), xDoc);
            var response = new AddTextResponse();
            response.setSessionId(sessionId);
            response.setTextId(docId.toString());
            response.setResponseText("File uploaded and added successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            
            var response = new AddTextResponse();
            response.setSessionId(sessionId);
            response.setResponseText("An error occurred while uploading the document: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search for a query in the vector space.
     *
     * @param searchRequest - query to search
     * @return - response entity
     */
    @PostMapping(value = "/{sessionId}/run", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search for relevant data based on the query", description = "Searches for relevant data in the vector space based on the query")
    public ResponseEntity<SearchResponse> search(@Schema(implementation = SearchRequest.class) @RequestBody SearchRequest searchRequest, @PathVariable("sessionId") String sessionId) {
        try {
            JSONObject response = vectorService.search(UUID.fromString(sessionId), searchRequest.getQuery());
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setSessionId(sessionId);
            searchResponse.setResponseText(response.toString());
            return new ResponseEntity<>(searchResponse, HttpStatus.OK);

        } catch (VectorSearchException ve) {
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setSessionId(sessionId);
            searchResponse.setResponseText("An error occurred while processing the query: " + ve.getMessage());
            return new ResponseEntity<>(searchResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setSessionId(sessionId);
            searchResponse.setResponseText("An unexpected error occurred while processing the query: " + searchRequest.getQuery() + " " + e.getMessage());
            return new ResponseEntity<>(searchResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            
        }
    }

}