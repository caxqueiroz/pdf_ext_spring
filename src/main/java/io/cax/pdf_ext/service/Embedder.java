package io.cax.pdf_ext.service;

import io.cax.pdf_ext.exception.EmbedderException;

/**
 * Embedder is a service that embeds text into a vector space.
 */
public interface Embedder {
    float[] embed(String text) throws EmbedderException;
}
