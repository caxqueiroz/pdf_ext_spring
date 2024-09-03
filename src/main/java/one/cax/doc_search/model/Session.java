package one.cax.doc_search.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A session.
 */
@Getter
public class Session {

    /* The session id */
    private UUID sessionId;

    /* The documents in the session */
    private List<XDoc> documents = new ArrayList<>();


    /**
     * Sets the session ID.
     *
     * @param sessionId The UUID to set as the session ID.
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public void addDocument(XDoc document) {
        this.documents.add(document);
    }
}