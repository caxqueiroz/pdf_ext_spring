package io.cax.pdf_ext.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
@ToString
@EqualsAndHashCode
public class XDoc {


    /* The id of the document */
    private UUID id;

    /* The title of the document */
    private String docTitle;

    /* The filename of the document */
    private String filename;

    /* The total number of pages in the document */
    private int totalPages;
    /* The metadata of the document */
    private HashMap<String, Object> metadata;

    /* The pages of the document */
    private List<XPage> pages;

    /**
     * Constructor for XDoc.
     */
    public XDoc() {
        this.id = UUID.randomUUID();
    }

    /**
     * Converts the XDoc to a JSONObject.
     * @return JSONObject, or null if the string is not a valid JSON
    */
    public JSONObject toJSON() throws JSONException {
        try {
            var json = new JSONObject();
            json.put(NameUtils.DOC_TITLE, this.getDocTitle());
            json.put(NameUtils.DOC_FILENAME, this.getFilename());
            json.put(NameUtils.DOC_TOTAL_PAGES, this.getTotalPages());
            
            var jsonArray = new JSONArray();
            for (XPage page : pages) {
                jsonArray.put(page.toJSON());
            }
            
            json.put(NameUtils.DOC_PAGES, jsonArray);
            return json; 
        } catch (JSONException e) {
            throw new JSONException("Error parsing JSON string: " + e.getMessage());
        }
    }

    /**
     * Converts a JSON string to an XDoc.
     * @param document - JSON string
     * @return XDoc
     * @throws JSONException
     */
    public static XDoc fromText(String document) throws JSONException {
       var json = new JSONObject(document);
        var xDoc = new XDoc();
        xDoc.setDocTitle(json.getString(NameUtils.DOC_TITLE));
        xDoc.setTotalPages(json.getInt(NameUtils.DOC_TOTAL_PAGES));
        xDoc.setPages(XPage.fromJSONArray(json.getJSONArray(NameUtils.DOC_PAGES)));
        return xDoc;
    }


}