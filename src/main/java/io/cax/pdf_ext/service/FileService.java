package io.cax.pdf_ext.service;

import io.cax.pdf_ext.exception.FileServiceException;
import io.cax.pdf_ext.model.NameUtils;
import io.cax.pdf_ext.model.XDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * FileService is a service that manages file operations.   
 */
@Service
public class FileService {

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
    public XDoc extractTextFrom(String filePath) throws FileServiceException {
        Path path = Paths.get(filePath);
        byte[] asBytes = null;
        try {
            asBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileServiceException("Error reading to memory: ",e);
        }
        return extractTextFrom(asBytes, NameUtils.APPLiCATION_PDF);
    }

    /**
     * Extract text from a file in bytes
     * @param fileInBytes - file in bytes
     * @param fileType - file type
     * @return XDoc object
     */
    public XDoc extractTextFrom(byte[] fileInBytes, String fileType) throws FileServiceException {

        try {
            return extractorEngine.extractTextFromPDF(fileInBytes);
        } catch (Exception e) {
            throw new FileServiceException("Error extracting text from file type: " + fileType, e);
        }
    }
    
}