package one.cax.doc_search;

import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.model.XPage;
import one.cax.doc_search.service.OpenAIEmbedderService;
import one.cax.doc_search.service.SessionService;
import one.cax.doc_search.service.VectorSearch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
@Tag("integration")
class VectorSearchIntegrationTests {

    @Autowired
    private OpenAIEmbedderService openAIEmbedderService;

    @Autowired
    private VectorSearch vectorSearch;

    @Autowired
    private SessionService sessionService;

    @Test
    void testAddDocumentAndSearch() throws VectorSearchException {
        // Create a session
        UUID sessionId = sessionService.createSession();

        // Create a test document
        XDoc testDoc = new XDoc();
        XPage page1 = new XPage();
        page1.setText("This is a test page about artificial intelligence.");
        XPage page2 = new XPage();
        page2.setText("Vector search is an efficient method for similarity search.");
        testDoc.setPages(Arrays.asList(page1, page2));

        // Add document to vector search
        UUID docId = vectorSearch.addDocument(sessionId, testDoc);
        assertNotNull(docId);

        // Perform a search
        String query = "What is artificial intelligence?";
        JSONObject searchResult = vectorSearch.search(sessionId, query);

        // Assert search results
        assertNotNull(searchResult);
        assertTrue(searchResult.has("results"));
        JSONArray results = searchResult.getJSONArray("results");
        assertFalse(results.isEmpty());

        // Check if the first result contains our test page
        JSONObject firstResult = results.getJSONObject(0);
        assertTrue(firstResult.getString("text").contains("artificial intelligence"));
    }

    @Test
    void testSearchWithNonExistentSession() {
        UUID nonExistentSessionId = UUID.randomUUID();
        String query = "Test query";

        assertThrows(VectorSearchException.class, () -> {
            vectorSearch.search(nonExistentSessionId, query);
        });
    }

    @Test
    void testSearchWithEmptyQuery() {
        UUID sessionId = sessionService.createSession();
        String emptyQuery = "";

        assertThrows(VectorSearchException.class, () -> {
            vectorSearch.search(sessionId, emptyQuery);
        });
    }

}
