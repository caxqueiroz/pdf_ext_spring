package io.cax.pdf_ext.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.UUID;

@Setter
@Getter
public class XDoc {

    /* The id of the document */
    private UUID id;

    /* The content of the document */
    private String content;

    /* The metadata of the document */
    private HashMap<String, Object> metadata;

    /* The vector representation of the document */
    private float[] vector;

}
