package io.cax.pdf_ext.service;

import io.github.jbellis.jvector.vector.types.VectorFloat;

public interface Embedder {
    VectorFloat<?> embed(String text);
}
