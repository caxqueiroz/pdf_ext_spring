package io.cax.pdf_ext.controller;

import io.cax.pdf_ext.model.XDoc;
import io.cax.pdf_ext.service.VectorSearch;
import io.cax.pdf_ext.service.VectorSearchException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController("/search")
public class VectorController {

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
    @PostMapping("/addDoc")
    public ResponseEntity<Object> addDocument(@RequestBody String document, @RequestHeader(SessionController.XSESSION) String sessionId) {
        try {
            var xDoc = new XDoc();
            xDoc.setContent(document);
            var docId = vectorService.addDocument(UUID.fromString(sessionId), xDoc);
            return new ResponseEntity<>(docId.toString(), HttpStatus.OK);
        } catch (VectorSearchException ve) {
            return new ResponseEntity<>("An error occurred while adding the document: " + ve.getMessage() + "!", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while adding the document: " + document + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Search for a query in the vector space.
     * @param query - query to search
     * @return - response entity
     */
    @PostMapping("/query")
    public ResponseEntity<Object> search(@RequestBody String query, @RequestHeader(SessionController.XSESSION) String sessionId) {
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
