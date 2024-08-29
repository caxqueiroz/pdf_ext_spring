package io.cax.pdf_ext.service;

public interface Embedder {
    float[] embed(String text) throws EmbedderException;
}
