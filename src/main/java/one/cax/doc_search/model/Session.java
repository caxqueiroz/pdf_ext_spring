package one.cax.doc_search.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a session that contains multiple documents.
 * This class encapsulates the concept of a user session, which can hold
 * multiple XDoc objects representing different documents.
 *
 * Key components:
 * - sessionId: A unique identifier for the session
 * - documents: A list of XDoc objects representing the documents in the session
 *
 * The Session class uses Lombok's @Getter annotation for automatic generation
 * of getter methods for its fields.
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