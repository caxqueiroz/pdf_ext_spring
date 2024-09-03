package one.cax.doc_search.service;

import one.cax.doc_search.exception.EmbedderException;

/**
 * Embedder is a service that embeds text into a vector space.
 */
public interface Embedder {
    float[] embed(String text) throws EmbedderException;
}
