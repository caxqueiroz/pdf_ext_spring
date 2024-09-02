package io.cax.pdf_ext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.cax.pdf_ext.model.NameUtils;
import io.cax.pdf_ext.model.XPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONArray;
import org.json.JSONObject;

public class TestUtils {

    public static byte[] createPdf(String content) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText(content);
            contentStream.endText();
            contentStream.close();

            document.save(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    public static JSONObject getJsonObject() {
        JSONObject jsonDoc = new JSONObject();
        jsonDoc.put(NameUtils.DOC_TITLE, "Test document title");
        jsonDoc.put(NameUtils.DOC_TOTAL_PAGES, 1);

        var xPage = new XPage();
        xPage.setPageNumber(1);
        xPage.setText("Test document content");
        var jsonArray = new JSONArray();
        jsonArray.put(xPage.toJSON());
        jsonDoc.put(NameUtils.DOC_PAGES, jsonArray);
        return jsonDoc;
    }

    /**
     * Get the URI for a search request.
     * @param sessionId
     * @param action
     * @return
     */
    public static String getURIForSearch(String sessionId, String action) {
        return String.format("/search/%s/%s", sessionId, action);
    }
}