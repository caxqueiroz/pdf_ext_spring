package io.cax.pdf_ext;

import io.cax.pdf_ext.model.Session;
import io.cax.pdf_ext.model.XDoc;
import io.cax.pdf_ext.service.OpenAIEmbedderService;
import io.cax.pdf_ext.service.SessionService;
import io.cax.pdf_ext.service.VectorSearch;
import io.cax.pdf_ext.service.VectorSearchException;
import io.github.jbellis.jvector.vector.VectorUtil;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VectorSearchTest {

    @Mock
    private OpenAIEmbedderService embedderService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private VectorSearch vectorSearch;

    private UUID sessionId;
    private XDoc document;
    private List<XDoc> documents;

    @BeforeEach
    public void setUp() {
        sessionId = UUID.randomUUID();
        document = new XDoc();
        documents = new ArrayList<>();
        documents.add(document);
    }

    @Test
    public void testAddDocument_Success() throws VectorSearchException {
        when(sessionService.sessionExists(sessionId)).thenReturn(true);
        Session session = mock(Session.class);
        when(sessionService.getSession(sessionId)).thenReturn(session);
        when(session.getDocuments()).thenReturn(documents);

        vectorSearch.addDocument(sessionId, document);

        verify(sessionService, times(1)).getSession(sessionId);
        assertEquals(2, documents.size());
    }

    @Test
    public void testAddDocument_SessionDoesNotExist() {
        when(sessionService.sessionExists(sessionId)).thenReturn(false);

        assertThrows(VectorSearchException.class, () -> vectorSearch.addDocument(sessionId, document));
    }

    @Test
    public void testSearch_Success() throws VectorSearchException, IOException {
        when(sessionService.sessionExists(sessionId)).thenReturn(true);
        Session session = mock(Session.class);
        when(sessionService.getSession(sessionId)).thenReturn(session);
        when(session.getDocuments()).thenReturn(documents);

        JSONObject result = vectorSearch.search(sessionId, "query");

        assertNotNull(result);
        assertTrue(result.has("results"));
    }

    @Test
    public void testSearch_SessionDoesNotExist() {
        when(sessionService.sessionExists(sessionId)).thenReturn(false);

        assertThrows(VectorSearchException.class, () -> vectorSearch.search(sessionId, "query"));
    }

    public static VectorFloat<?> randomVector(int dim) {
        VectorTypeSupport vts = VectorizationProvider.getInstance().getVectorTypeSupport();
        Random R = ThreadLocalRandom.current();
        VectorFloat<?> vec = vts.createFloatVector(dim);
        for (int i = 0; i < dim; i++) {
            vec.set(i, R.nextFloat());
            if (R.nextBoolean()) {
                vec.set(i, -vec.get(i));
            }
        }
        VectorUtil.l2normalize(vec);
        return vec;
    }
}