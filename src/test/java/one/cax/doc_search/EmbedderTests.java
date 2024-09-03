package one.cax.doc_search;


import one.cax.doc_search.exception.EmbedderException;
import one.cax.doc_search.service.OpenAIEmbedderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class EmbedderTests {


    @Mock
    private OpenAIEmbedderService openAIEmbedderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void embedReturnsNonNullArray() throws EmbedderException {
        String text = "sample text";
        float[] expected = new float[]{1.0f, 2.0f, 3.0f};
        when(openAIEmbedderService.embed(text)).thenReturn(expected);

        float[] result = openAIEmbedderService.embed(text);

        assertNotNull(result);
        assertArrayEquals(expected, result);
    }

    @Test
    void embedThrowsExceptionForNullText() throws EmbedderException {
        doThrow(new EmbedderException("Text cannot be null")).when(openAIEmbedderService).embed(null);
        assertThrows(EmbedderException.class, () -> openAIEmbedderService.embed(null));
    }

    @Test
    void embedThrowsExceptionForEmptyText() throws EmbedderException {
        doThrow(new EmbedderException("Text cannot be empty")).when(openAIEmbedderService).embed("");
        assertThrows(EmbedderException.class, () -> openAIEmbedderService.embed(""));
    }

    @Test
    void embedHandlesSpecialCharacters() throws EmbedderException {
        String text = "!@#$%^&*()";
        float[] expected = new float[]{4.0f, 5.0f, 6.0f};
        when(openAIEmbedderService.embed(text)).thenReturn(expected);

        float[] result = openAIEmbedderService.embed(text);

        assertNotNull(result);
        assertArrayEquals(expected, result);
    }
}