package io.cax.pdf_ext.service;

import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.cax.pdf_ext.exception.FileServiceException;
import io.cax.pdf_ext.model.XDoc;

/**
 * FileService is a service that manages file operations.   
 */
@Service
public class FileService {

    private final Logger logger = Logger.getLogger(FileService.class.getName());

    @Value("${doc_ext_search.temp_folder}")
    private final ExtractorEngine extractorEngine;

    @Autowired
    public FileService(ExtractorEngine extractorEngine) {
        this.extractorEngine = extractorEngine;
    }

    /**
     * Extract text from a file
     * @param filePath - path to the file
     * @return text
     */
    public String extractTextFrom(String filePath) {
        return null;
    }

    /**
     * Extract text from a file in bytes
     * @param fileInBytes - file in bytes
     * @param fileType - file type
     * @return XDoc object
     */
    public XDoc extractTextFrom(byte[] fileInBytes, String fileType) throws FileServiceException {

        try {

            switch (fileType) {
                case "pdf":
                    return extractorEngine.extractTextFromPDF(fileInBytes);
                default:
                    return extractorEngine.extractTextFromPDF(fileInBytes);
            }
            
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new FileServiceException("Error extracting text from file type: " + fileType, e);
        }
    }
    
}
