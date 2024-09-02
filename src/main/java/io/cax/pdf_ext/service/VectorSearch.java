package io.cax.pdf_ext.service;

import io.cax.pdf_ext.exception.EmbedderException;
import io.cax.pdf_ext.exception.VectorSearchException;
import io.cax.pdf_ext.model.XDoc;
import io.github.jbellis.jvector.graph.*;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;
import io.github.jbellis.jvector.graph.similarity.SearchScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
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
import java.util.stream.Collectors;

/**
 * Class that represents the vector search service.
 */
@Service
public class VectorSearch {
    private static final Logger logger = Logger.getLogger(VectorSearch.class.getName());
    private final static VectorTypeSupport vts = VectorizationProvider.getInstance().getVectorTypeSupport();

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
     * @param sessionId - the session id
     * @param document - the document to add
     * @throws VectorSearchException - if an error occurs
     * @return - the document id
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
            session.getDocuments().add(document);

        } else {
            logger.warning("Session does not exist");
            throw new VectorSearchException("Session does not exist!");
        }
        if (processedPages.get() != document.getTotalPages()){
            throw new VectorSearchException(String.format("Only %d out of %d pages were processed", processedPages.get(), document.getTotalPages()));
        }
        return document.getId();

    }
    /**
     * Search for a query in the vector space.
     * @param sessionId - the session id
     * @param query - the query
     * @return - the search result
     * @throws VectorSearchException - if an error occurs
     */
    public JSONObject search(UUID sessionId, String query) throws VectorSearchException, JSONException, IOException {

        if (sessionService.sessionExists(sessionId)) {
            if (query == null || query.isEmpty()) {
                throw new VectorSearchException("Query is empty!");
            }
            var session = sessionService.getSession(sessionId);
            float[] embeddedQuery = null;
            try {
                embeddedQuery = embedderService.embed(query);
            } catch (EmbedderException e) {
                throw new RuntimeException(e);
            }
            var docs = session.getDocuments();
            List<VectorFloat<?>> vectorArray = docs.stream().flatMap(doc -> doc.getPages().stream())
            .map(xPage -> xPage.getVector()) 
            .map(vts::createFloatVector) // Create a float vector
            .collect(Collectors.toList());

            if (vectorArray.isEmpty()) {
                throw new VectorSearchException("No documents to search!");
            }

            int originalDimension = vectorArray.get(0).length();
            RandomAccessVectorValues ravv = new ListRandomAccessVectorValues(vectorArray, originalDimension);
            var vQuery= vts.createFloatVector(embeddedQuery);
            BuildScoreProvider bsp = BuildScoreProvider.randomAccessScoreProvider(ravv, VectorSimilarityFunction.valueOf(similarityFunctionName));
            try (GraphIndexBuilder builder = new GraphIndexBuilder(bsp, ravv.dimension(), 16, 100, 1.2f, 1.2f)) {
                OnHeapGraphIndex index = builder.build(ravv);

                try (GraphSearcher searcher = new GraphSearcher(index)) {
                    SearchScoreProvider ssp = SearchScoreProvider.exact(vQuery, VectorSimilarityFunction.valueOf(similarityFunctionName), ravv);
                    SearchResult sr = searcher.search(ssp, topK, Bits.ALL);
                    return convertSearchResult(sr, docs);
                }
            }

        } else {
            throw new VectorSearchException("Session does not exist!");
        }
    }

    /**
     * Convert a search result to a JSON object.
     * @param sr - the search result
     * @param docs - the documents
     * @return - the JSON object
     */
    private JSONObject convertSearchResult(SearchResult sr, List<XDoc> docs) throws JSONException {

        List<XDoc> results = new ArrayList<>();
        for (SearchResult.NodeScore ns : sr.getNodes()) {
            results.add(docs.get(ns.node));
        }
        JSONObject response = new JSONObject();
        response.put("results", results);
        return response;
    }
}
