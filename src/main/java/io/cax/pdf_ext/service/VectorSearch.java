package io.cax.pdf_ext.service;

import io.cax.pdf_ext.model.XDoc;
import io.github.jbellis.jvector.graph.*;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;
import io.github.jbellis.jvector.graph.similarity.SearchScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class that represents the vector search service.
 */
@Service
public class VectorSearch {

    /* The top K results to return */
    @Value("${topK}")
    private int topK;

    /* The similarity function to use */
    @Value("${vector.similarity.function}")
    private String similarityFunctionName;

    /* The EmbedderService - creates embeddings from talking to the LLM model */
    private OpenAIEmbedderService embedderService;

    /* The SessionService - manages the user sessions */
    private SessionService sessionService;


    /**
     * Create a new VectorSearch.
     */
    public VectorSearch() {
    }

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
     */
    public void addDocument(UUID sessionId, XDoc document) throws VectorSearchException {

        if (sessionService.sessionExists(sessionId)) {
            var embeddedDocument = embedderService.embed(document.getContent());
            var session = sessionService.getSession(sessionId);
            session.getDocuments().add(document);
        } else {
            throw new VectorSearchException("Session does not exist!");
        }

    }
    /**
     * Search for a query in the vector space.
     * @param sessionId - the session id
     * @param query - the query
     * @return - the search result
     * @throws VectorSearchException - if an error occurs
     */
    public JSONObject search(UUID sessionId, String query) throws VectorSearchException, IOException {

        if (sessionService.sessionExists(sessionId)) {

            var session = sessionService.getSession(sessionId);
            var embeddedQuery = embedderService.embed(query);
            var docs = session.getDocuments();
            List<VectorFloat<?>> vectorArray = docs.stream().map(XDoc::getVector).collect(Collectors.toList());
            int originalDimension = docs.get(0).getVector().length();
            RandomAccessVectorValues ravv = new ListRandomAccessVectorValues(vectorArray, originalDimension);

            BuildScoreProvider bsp = BuildScoreProvider.randomAccessScoreProvider(ravv, VectorSimilarityFunction.valueOf(similarityFunctionName));
            try (GraphIndexBuilder builder = new GraphIndexBuilder(bsp, ravv.dimension(), 16, 100, 1.2f, 1.2f)) {
                OnHeapGraphIndex index = builder.build(ravv);

                try (GraphSearcher searcher = new GraphSearcher(index)) {
                    SearchScoreProvider ssp = SearchScoreProvider.exact(embeddedQuery, VectorSimilarityFunction.valueOf(similarityFunctionName), ravv);
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
    private JSONObject convertSearchResult(SearchResult sr, List<XDoc> docs) {

        List<XDoc> results = new ArrayList<>();
        for (SearchResult.NodeScore ns : sr.getNodes()) {
            results.add(docs.get(ns.node));
        }
        JSONObject response = new JSONObject();
        response.put("results", results);
        return response;
    }
}
