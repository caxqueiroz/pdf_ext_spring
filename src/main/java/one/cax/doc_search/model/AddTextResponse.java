package one.cax.doc_search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Getter
@Setter
public class AddTextResponse {

    /* SessionId the text has been added to */
    @Schema(description = "the sessionId used")
    private String sessionId; 

    /**
     * The unique identifier of the document that was added.
     * This ID can be used for future reference or retrieval of the document.
     */
    @Schema(description = "The unique identifier of the document that was added")
    private String textId;

    /**
     * A message indicating the status of the text addition operation.
     * This could be a success message or any other relevant information.
     */
    @Schema(description = "A message indicating the status of the text addition operation")
    private String responseText;
    
    /**
     * Converts this AddTextResponse object to a JSON string representation.
     *
     * @return A JSON string containing the sessionId, textId, and responseText.
     */
    public String toJSON() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"sessionId\":\"").append(sessionId).append("\",");
        json.append("\"textId\":\"").append(textId).append("\",");
        json.append("\"responseText\":\"").append(responseText).append("\"");
        json.append("}");
        return json.toString();
    }
}
