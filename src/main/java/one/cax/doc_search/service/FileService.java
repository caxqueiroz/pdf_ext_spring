package one.cax.doc_search.service;

import one.cax.doc_search.exception.FileServiceException;
import one.cax.doc_search.model.NameUtils;
import one.cax.doc_search.model.XDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * FileService is responsible for handling file operations and text extraction.
 * 
 * This service provides methods to extract text from files, either by file path
 * or directly from byte arrays. It uses an ExtractorEngine to perform the actual
 * text extraction, supporting various file types including PDF.
 * 
 * Key functionalities:
 * - Extracting text from files specified by file path
 * - Extracting text from files provided as byte arrays
 * - Handling different file types (currently supporting PDF)
 * 
 * @author Carlos Queiroz
 * @version 1.0
 * @since 2024-09-02
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
     *
     * @param filePath - path to the file
     * @return text
     */
    public XDoc extractTextFrom(String filePath) throws FileServiceException {
        Path path = Paths.get(filePath);
        byte[] asBytes = null;
        try {
            asBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FileServiceException("Error reading to memory: ", e);
        }
        return extractTextFrom(asBytes, NameUtils.APPLICATION_PDF);
    }

    /**
     * Extract text from a file in bytes
     *
     * @param fileInBytes - file in bytes
     * @param fileType    - file type
     * @return XDoc object
     * 
     */
    public XDoc extractTextFrom(byte[] fileInBytes, String fileType) throws FileServiceException {

        try {
            return extractorEngine.extractTextFromPDF(fileInBytes);
        } catch (Exception e) {
            throw new FileServiceException("Error extracting text from file type: " + fileType, e);
        }
    }

}