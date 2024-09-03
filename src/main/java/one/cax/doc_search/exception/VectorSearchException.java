package one.cax.doc_search.exception;

public class VectorSearchException extends Exception {
    public VectorSearchException(String message) {
        super(message);
    }

    public VectorSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public VectorSearchException(Throwable cause) {
        super(cause);
    }

}
