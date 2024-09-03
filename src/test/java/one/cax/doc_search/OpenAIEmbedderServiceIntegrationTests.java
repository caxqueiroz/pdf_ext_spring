package one.cax.doc_search;


import one.cax.doc_search.exception.EmbedderException;
import one.cax.doc_search.service.OpenAIEmbedderService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class OpenAIEmbedderServiceIntegrationTests {

    @Autowired
    private OpenAIEmbedderService openAIEmbedderService;

   @Test
   @Tag("integration")
    void testEmbedReturnsNonNullArray() throws EmbedderException {
        String text = "sample text";
        float[] result = openAIEmbedderService.embed(text);
        assertNotNull(result);
    }
}
