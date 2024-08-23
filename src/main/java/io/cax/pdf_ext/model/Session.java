package io.cax.pdf_ext.model;

import io.github.jbellis.jvector.vector.types.VectorFloat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * A session.
 */
@Getter
@Setter
public class Session {

    /* The session id */
    private UUID sessionId;

    /* The documents in the session */
    private List<XDoc> documents;



}