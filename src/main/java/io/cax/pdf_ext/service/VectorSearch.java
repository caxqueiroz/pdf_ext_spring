package io.cax.pdf_ext.service;

import io.cax.pdf_ext.model.XDoc;
import io.github.jbellis.jvector.graph.*;
import io.github.jbellis.jvector.graph.similarity.SearchScoreProvider;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

@Service
public class VectorSearch {

    @Value("${topK}")
    private int topK;

    @Value("${vector.similarity.function}")
    private String similarityFunctionName;

    private EmbedderService embedderService;

    private SessionService sessionService;

    private GraphIndexBuilder graphIndex;

    /**
     * Create a new VectorSearch.
     */
    public VectorSearch() {
    }

    /**
     * Add a document to the vector space.
     * @param sessionId
     * @param document
     * @throws VectorSearchException
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
     * @param sessionId
     * @param query
     * @return
     * @throws VectorSearchException
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
     * @param sr
     * @param docs
     * @return
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
