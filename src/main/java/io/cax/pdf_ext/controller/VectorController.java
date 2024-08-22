package io.cax.pdf_ext.controller;

import io.cax.pdf_ext.service.VectorSearch;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController("/search")
public class VectorController {

    private final VectorSearch vectorService;

    @Autowired
    public VectorController(VectorSearch vectorSearch) {
        this.vectorService = vectorSearch;
    }

    @PostMapping
    public ResponseEntity<Object> search(@RequestBody String query) {
        try {
            JSONObject response = vectorService.search(query);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while processing the query: " + query + "!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
