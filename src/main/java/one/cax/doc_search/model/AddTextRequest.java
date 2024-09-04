package one.cax.doc_search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTextRequest {
    @Schema(description = "The title of the document")
    private String title;

    @Schema(description = "The content of the document", example = "This is the content of the sample document.")
    private String content;

    /**
     * Returns the content of the document
     * @return
     */
    public String toString(){
        return this.content;
    }
}
