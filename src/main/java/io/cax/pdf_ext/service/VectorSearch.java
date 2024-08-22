package io.cax.pdf_ext.service;

import io.github.jbellis.jvector.graph.GraphIndex;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import io.github.jbellis.jvector.graph.similarity.BuildScoreProvider;

import java.util.Vector;

@Service
public class VectorSearch {

    private GraphIndexBuilder graphIndex;

    /**
     * Create a new VectorSearch.
     */
    public VectorSearch() {
        // score provider using the raw, in-memory vectors
        BuildScoreProvider bsp = BuildScoreProvider.randomAccessScoreProvider(ravv, VectorSimilarityFunction.EUCLIDEAN);
        this.graphIndex = new GraphIndexBuilder(bsp.searchProviderFor());
    }
    /*
        * Add a document to the vector space.
     */
    public void addDocument(VectorFloat<?> document) {

        graphIndex.addGraphNode()
    }

    /**
     * Search for a query in the vector space.
     * @param query
     * @return
     */
    public JSONObject search(VectorFloat<?> query) {

        SearchResult sr = GraphSearcher.search(query, // query vector
                10, // number of results
                ravv, // vectors we're searching, used for scoring
                VectorSimilarityFunction.EUCLIDEAN, // how to score
                index,
                Bits.ALL); // valid ordinals to consider
    }
}
