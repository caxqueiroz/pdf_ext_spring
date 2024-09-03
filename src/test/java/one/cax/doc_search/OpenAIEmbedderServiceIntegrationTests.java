package one.cax.doc_search;

import one.cax.doc_search.exception.EmbedderException;
import one.cax.doc_search.service.OpenAIEmbedderService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
@Tag("integration")
class OpenAIEmbedderServiceIntegrationTests {

    @Autowired
    private OpenAIEmbedderService openAIEmbedderService;

    @Test
    void testEmbedderServiceCreation() {
        assertNotNull(openAIEmbedderService);
    }

    @Test
    void testEmbedReturnsValidEmbedding() throws EmbedderException {
        var text = "The quick brown fox jumps over the lazy dog";
        float[] result = openAIEmbedderService.embed(text);
        assertNotNull(result);
        assertEquals(3072, result.length, "Embedding should have 3072 dimensions");
    }

    @Test
    void testEmbededReturnsBatchValid() throws EmbedderException {
        var texts = List.of(
                "The quick brown fox jumps over the lazy dog",
                "Lorem ipsum dolor sit amet",
                "OpenAI is revolutionizing artificial intelligence"
        );

        float[][] results = openAIEmbedderService.embedBatch(texts);

        assertNotNull(results);
        assertEquals(3, results.length, "Should return embeddings for all 3 input texts");

        for (float[] embedding : results) {
            assertEquals(3072, embedding.length, "Each embedding should have 3072 dimensions");
        }
    }

}
