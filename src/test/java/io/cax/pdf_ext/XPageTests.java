package io.cax.pdf_ext;

import io.cax.pdf_ext.model.NameUtils;
import io.cax.pdf_ext.model.XPage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class XPageTests {

    @Test
    void testToJSON() throws Exception {
        XPage xPage = new XPage();
        xPage.setPageNumber(1);
        xPage.setText("Sample text");
        xPage.setVector(new float[]{1.0f, 2.0f, 3.0f});

        JSONObject json = xPage.toJSON();

        assertEquals(1, json.getInt(NameUtils.PAGE_NUMBER));
        assertEquals("Sample text", json.getString(NameUtils.PAGE_TEXT));
        JSONArray vectorArray = json.getJSONArray(NameUtils.PAGE_VECTOR);
        assertEquals(3, vectorArray.length());
        assertEquals(1.0, vectorArray.getDouble(0));
        assertEquals(2.0, vectorArray.getDouble(1));
        assertEquals(3.0, vectorArray.getDouble(2));
    }

    @Test
    void testFromJSON() throws Exception {
        JSONObject json = new JSONObject();
        json.put(NameUtils.PAGE_NUMBER, 1);
        json.put(NameUtils.PAGE_TEXT, "Sample text");
        json.put(NameUtils.PAGE_VECTOR, new JSONArray(new float[]{1.0f, 2.0f, 3.0f}));

        XPage xPage = XPage.fromJSON(json);

        assertEquals(1, xPage.getPageNumber());
        assertEquals("Sample text", xPage.getText());
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, xPage.getVector());
    }

    @Test
    void testFromJSONArray() throws Exception {
        JSONObject json1 = new JSONObject();
        json1.put(NameUtils.PAGE_NUMBER, 1);
        json1.put(NameUtils.PAGE_TEXT, "Sample text 1");
        json1.put(NameUtils.PAGE_VECTOR, new JSONArray(new float[]{1.0f, 2.0f, 3.0f}));

        JSONObject json2 = new JSONObject();
        json2.put(NameUtils.PAGE_NUMBER, 2);
        json2.put(NameUtils.PAGE_TEXT, "Sample text 2");
        json2.put(NameUtils.PAGE_VECTOR, new JSONArray(new float[]{4.0f, 5.0f, 6.0f}));

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json1);
        jsonArray.put(json2);

        var xPages = XPage.fromJSONArray(jsonArray);

        assertEquals(2, xPages.size());

        XPage xPage1 = xPages.get(0);
        assertEquals(1, xPage1.getPageNumber());
        assertEquals("Sample text 1", xPage1.getText());
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, xPage1.getVector());

        XPage xPage2 = xPages.get(1);
        assertEquals(2, xPage2.getPageNumber());
        assertEquals("Sample text 2", xPage2.getText());
        assertArrayEquals(new float[]{4.0f, 5.0f, 6.0f}, xPage2.getVector());
    }
}
