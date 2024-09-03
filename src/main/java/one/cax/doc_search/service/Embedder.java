package one.cax.doc_search.service;

import one.cax.doc_search.exception.EmbedderException;

import java.util.List;

/**
 * Embedder is a service that embeds text into a vector space.
 */
public interface Embedder {
    /**
     * Embeds the given text into a vector representation.
     *
     * @param text The input text to be embedded.
     * @return A float array representing the embedded vector of the input text.
     * @throws EmbedderException If an error occurs during the embedding process.
     */
    float[] embed(String text) throws EmbedderException;

    /**
     * Embeds the given texts into a vector representation.
     * @param texts
     * @return
     * @throws EmbedderException
     */
    float[][] embedBatch(List<String> texts) throws EmbedderException;
}
