package one.cax.doc_search;

import one.cax.doc_search.model.NameUtils;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.model.XPage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XDocTests {

    @Test
    void testToJSON() throws Exception {
        XDoc xDoc = new XDoc();
        xDoc.setDocTitle("Test Title");
        xDoc.setFilename("test.pdf");
        xDoc.setTotalPages(2);
        xDoc.setMetadata(new HashMap<>());

        XPage page1 = new XPage();
        page1.setText("Page 1 text");
        XPage page2 = new XPage();
        page2.setText("Page 2 text");

        xDoc.setPages(List.of(page1, page2));

        JSONObject json = xDoc.toJSON();

        assertNotNull(json);
        assertEquals("Test Title", json.getString(NameUtils.DOC_TITLE));
        assertEquals("test.pdf", json.getString(NameUtils.DOC_FILENAME));
        assertEquals(2, json.getInt(NameUtils.DOC_TOTAL_PAGES));

        JSONArray pages = json.getJSONArray(NameUtils.DOC_PAGES);
        assertEquals(2, pages.length());
        assertEquals("Page 1 text", pages.getJSONObject(0).getString(NameUtils.PAGE_TEXT));
        assertEquals("Page 2 text", pages.getJSONObject(1).getString(NameUtils.PAGE_TEXT));
    }

    @Test
    void testFromText() throws Exception {
        String documentText = "{\"docTitle\":\"Test Title\",\"docTotalPages\":2,\"docPages\":[{\"pageText\":\"Page 1 text\",\"pageNumber\":1},{\"pageText\":\"Page 2 text\",\"pageNumber\":2}]}";
        XDoc xDoc = XDoc.fromText(documentText);

        assertNotNull(xDoc);
        assertEquals("Test Title", xDoc.getDocTitle());
        assertEquals(2, xDoc.getTotalPages());

        List<XPage> pages = xDoc.getPages();
        assertEquals(2, pages.size());
        assertEquals("Page 1 text", pages.get(0).getText());
        assertEquals("Page 2 text", pages.get(1).getText());
    }

    @Test
    void testGetPages() {
        XDoc xDoc = new XDoc();
        XPage page1 = new XPage();
        page1.setText("Page 1 text");
        XPage page2 = new XPage();
        page2.setText("Page 2 text");

        xDoc.setPages(List.of(page1, page2));

        List<XPage> pages = xDoc.getPages();
        assertEquals(2, pages.size());
        assertEquals("Page 1 text", pages.get(0).getText());
        assertEquals("Page 2 text", pages.get(1).getText());
    }
}
