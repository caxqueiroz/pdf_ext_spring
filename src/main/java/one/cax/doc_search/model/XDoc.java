package one.cax.doc_search.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a document extracted from a file, typically a PDF.
 * This class encapsulates all the information related to a document,
 * including its metadata, content, and pages.
 *
 * The XDoc class uses Lombok annotations for automatic generation of
 * getters, setters, toString, equals, and hashCode methods.
 *
 * Key components:
 * - id: A unique identifier for the document
 * - docTitle: The title of the document
 * - filename: The name of the file from which the document was extracted
 * - metadata: Additional information about the document
 * - pages: The content of the document, divided into pages
 */
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


    /* The metadata of the document */
    private HashMap<String, Object> metadata;

    /* The pages of the document */
    private AtomicReference<List<XPage>> pages = new AtomicReference<>(new ArrayList<>());

    /**
     * Constructor for XDoc.
     */
    public XDoc() {
        this.id = UUID.randomUUID();
    }

    /**
     * Converts a JSON string to an XDoc.
     *
     * @param document - JSON string
     * @return XDoc
     * @throws JSONException
     */
    public static XDoc fromText(String document) throws JSONException {
        var json = new JSONObject(document);
        var xDoc = new XDoc();
        xDoc.setDocTitle(json.getString(NameUtils.DOC_TITLE));
        xDoc.setPages(XPage.fromJSONArray(json.getJSONArray(NameUtils.DOC_PAGES)));
        return xDoc;
    }

    /**
     * @return
     */
    public List<XPage> getPages() {
        return this.pages.get();
    }

    /**
     * Set the pages of the document.
     *
     * @param pagesList
     */
    public void setPages(List<XPage> pagesList) {
        this.pages.set(pagesList != null ? new ArrayList<>(pagesList) : new ArrayList<>());
    }

    /**
     * returns the number of pages in the document.
     *
     * @return
     */
    public int getTotalPages() {
        return this.pages.get().size();
    }

    /**
     * Converts the XDoc to a JSONObject.
     *
     * @return JSONObject, or null if the string is not a valid JSON
     */
    public JSONObject toJSON() throws JSONException {
        try {
            var json = new JSONObject();
            json.put(NameUtils.DOC_TITLE, this.getDocTitle());
            json.put(NameUtils.DOC_FILENAME, this.getFilename());
            json.put(NameUtils.DOC_TOTAL_PAGES, this.getTotalPages());

            var jsonArray = new JSONArray();
            if (pages != null) {
                for (XPage page : pages.get()) {
                    jsonArray.put(page.toJSON());
                }
            }
            json.put(NameUtils.DOC_PAGES, jsonArray);
            return json;
        } catch (JSONException e) {
            throw new JSONException("Error parsing JSON string: " + e.getMessage());
        }
    }


}