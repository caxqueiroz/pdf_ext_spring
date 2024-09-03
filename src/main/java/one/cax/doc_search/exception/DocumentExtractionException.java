package one.cax.doc_search.exception;

public class DocumentExtractionException extends Exception {

    public DocumentExtractionException(String message) {
        super(message);
    }

    public DocumentExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentExtractionException(Throwable cause) {
        super(cause);
    }

}
