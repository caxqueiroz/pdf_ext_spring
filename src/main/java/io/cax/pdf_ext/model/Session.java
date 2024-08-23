package io.cax.pdf_ext.model;

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