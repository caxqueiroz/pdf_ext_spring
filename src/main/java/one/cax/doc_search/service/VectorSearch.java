package one.cax.doc_search.service;

import io.github.jbellis.jvector.graph.*;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;
import io.github.jbellis.jvector.graph.similarity.SearchScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import one.cax.doc_search.exception.EmbedderException;
import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.model.XPage;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


/**
 * VectorSearch is responsible for managing vector-based document search operations.
 * 
 * This service provides methods to add documents to a vector space, perform similarity
 * searches, and manage the underlying vector representations of documents and queries.
 * It integrates with OpenAIEmbedderService for generating embeddings and SessionService
 * for managing user sessions.
 * 
 * Key functionalities:
 * - Adding documents to the vector space
 * - Performing similarity searches on the vector space
 * - Managing document embeddings and search configurations
 * 
 * The service uses configurable parameters for top-K results and similarity functions,
 * allowing for flexible and customizable search behavior.
 */
@Service
public class VectorSearch {
    private static final Logger logger = Logger.getLogger(VectorSearch.class.getName());
    private static final VectorTypeSupport vts = VectorizationProvider.getInstance().getVectorTypeSupport();

    /* The top K results to return */
    @Value("${doc_ext_search.topK}")
    private int topK;

    /* The similarity function to use */
    @Value("${doc_ext_search.similarity.function}")
    private String similarityFunctionName;

    /* The EmbedderService - creates embeddings from talking to the LLM model */
    private OpenAIEmbedderService embedderService;

    /* The SessionService - manages the user sessions */
    private SessionService sessionService;

    @Autowired
    public void setEmbedderService(OpenAIEmbedderService embedderService) {
        this.embedderService = embedderService;
    }

    @Autowired
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Add a document to the vector space.
     *
     * @param sessionId - the session id
     * @param document  - the document to add
     * @return - the document id
     * @throws VectorSearchException - if an error occurs
     */
    public UUID addDocument(UUID sessionId, XDoc document) throws VectorSearchException {

        AtomicInteger processedPages = new AtomicInteger(0);

        if (sessionService.sessionExists(sessionId)) {

            document.getPages().forEach(p -> {
                try {
                    p.setVector(embedderService.embed(p.getText()));
                    processedPages.incrementAndGet();
                } catch (EmbedderException e) {
                    logger.warning("error creating the embedding for a page");
                }
            });

            var session = sessionService.getSession(sessionId);
            session.addDocument(document);

        } else {
            logger.warning("Session does not exist");
            throw new VectorSearchException("Session does not exist!");
        }
        if (processedPages.get() != document.getTotalPages()) {
            throw new VectorSearchException(String.format("Only %d out of %d pages were processed", processedPages.get(), document.getTotalPages()));
        }
        return document.getId();

    }

    /**
     * Search for a query in the vector space.
     *
     * @param sessionId - the session id
     * @param query     - the query
     * @return - the search result
     * @throws VectorSearchException - if an error occurs
     */
    public JSONObject search(UUID sessionId, String query) throws VectorSearchException {

        if (sessionService.sessionExists(sessionId)) {
            if (query == null || query.isEmpty()) {
                throw new VectorSearchException("Query is empty!");
            }
            var session = sessionService.getSession(sessionId);
            float[] embeddedQuery = null;
            try {
                embeddedQuery = embedderService.embed(query);
            } catch (EmbedderException e) {
                throw new VectorSearchException("Error embedding the query: " + e.getMessage(), e);
            }
            var docs = session.getDocuments();
            List<VectorFloat<?>> vectorArray = new ArrayList<>();
            List<PageInfo> pageInfoList = new ArrayList<>();

            for (XDoc doc : docs) {
                for (XPage page : doc.getPages()) {
                    vectorArray.add(vts.createFloatVector(page.getVector()));
                    pageInfoList.add(new PageInfo(doc.getId(), page.getPageNumber(), page.getText()));
                }
            }

            if (vectorArray.isEmpty()) {
                throw new VectorSearchException("No documents to search!");
            }

            int originalDimension = vectorArray.get(0).length();
            RandomAccessVectorValues ravv = new ListRandomAccessVectorValues(vectorArray, originalDimension);
            var vQuery = vts.createFloatVector(embeddedQuery);
            BuildScoreProvider bsp = BuildScoreProvider.randomAccessScoreProvider(ravv, VectorSimilarityFunction.valueOf(similarityFunctionName));

            try (GraphIndexBuilder builder = new GraphIndexBuilder(bsp, ravv.dimension(), 16, 100, 1.2f, 1.2f)) {
                OnHeapGraphIndex index = builder.build(ravv);

                try (GraphSearcher searcher = new GraphSearcher(index)) {
                    SearchScoreProvider ssp = SearchScoreProvider.exact(vQuery, VectorSimilarityFunction.valueOf(similarityFunctionName), ravv);
                    SearchResult sr = searcher.search(ssp, topK, Bits.ALL);
                    return convertSearchResult(sr, pageInfoList);
                }
            } catch (IOException e) {
                throw new VectorSearchException("Error building the graph index: " + e.getMessage(), e);
            }

        } else {
            throw new VectorSearchException("Session does not exist!");
        }
    }

    /**
     * Convert a search result to a JSON object.
     *
     * @param sr   - the search result
     * @param pageInfoList - the list of page info
     * @return - the JSON object
     */
    private JSONObject convertSearchResult(SearchResult sr, List<PageInfo> pageInfoList) throws JSONException {

        JSONObject response = new JSONObject();
        List<JSONObject> results = new ArrayList<>();

        for (SearchResult.NodeScore ns : sr.getNodes()) {
            PageInfo pageInfo = pageInfoList.get(ns.node);
            JSONObject result = new JSONObject();
            result.put("docId", pageInfo.docId);
            result.put("pageNumber", pageInfo.pageNumber);
            result.put("score", ns.score);
            result.put("text", pageInfo.text);
            results.add(result);
        }

        response.put("results", results);
        return response;
    }

    private static class PageInfo {
        UUID docId;
        int pageNumber;
        String text;

        PageInfo(UUID docId, int pageNumber, String text) {
            this.docId = docId;
            this.pageNumber = pageNumber;
            this.text = text;
        }
    }
}

