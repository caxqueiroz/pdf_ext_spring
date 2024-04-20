package io.cax.pdf_ext.controller;


import io.cax.pdf_ext.service.ExtractorEngine;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/extract")
public class ExtractController {

    private final Logger logger = Logger.getLogger(ExtractController.class.getName());

    @Value("${pdf_ext.temp_folder}")
    private String tempFolder;

    private final ExtractorEngine extractorEngine;

    @Autowired
    public ExtractController(ExtractorEngine extractorEngine) {
        this.extractorEngine = extractorEngine;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please select a file!", HttpStatus.OK);
            }

            // save file temp folder
            String tempFilePath = tempFolder + file.getOriginalFilename();
            file.transferTo(new File(tempFilePath));

            JSONObject response = extractorEngine.extractTextFrom(tempFilePath);
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);

        } catch (IOException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>("Could not upload the file: " + file.getOriginalFilename() + "!", HttpStatus.EXPECTATION_FAILED);
        }
    }

}