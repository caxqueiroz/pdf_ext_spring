package one.cax.doc_search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequest {
    
    /**
     * The query string to be used for searching.
     * This represents the user's input or search terms.
     */
    @Schema(description = "query string to be used for searching")
    private String query;
    /**
     * The number of top results to return.
     * This limits the search results to the most relevant entries.
     */
    @Schema(description = "number of top results to return")
    private int topK;

}
