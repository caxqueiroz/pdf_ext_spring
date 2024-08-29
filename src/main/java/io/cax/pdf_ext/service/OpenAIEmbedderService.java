package io.cax.pdf_ext.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIEmbedderService implements Embedder {

    /* The OpenAI API URL */
    @Value("${openai.api.url}")
    private String openaiApiUrl;

    /* The OpenAI API key */
    @Value("${openai.api.key}")
    private String apiKey;

    /* The OpenAI API model */
    @Value("${openai.api.model}")
    private String model;

    /**
     * Embed a text using OpenAI API
     *
     * @param text - the text to embed
     * @return - the embedded text as VectorFloat object
     */
    public float[] embed(String text) throws EmbedderException {

        if (text == null || text.isEmpty()) {
            throw new EmbedderException("Text cannot be null or empty");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        try {

            requestBody.put("input", text);
            requestBody.put("model", model);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, entity, String.class);
            JSONObject responseBody = null;

            responseBody = new JSONObject(response.getBody());
            JSONObject embedding = responseBody.getJSONArray("data").getJSONObject(0).getJSONObject("embedding");
            // Convert the embedding to VectorFloat
            float[] vectorArray = new float[embedding.length()];
            for (int i = 0; i < embedding.length(); i++) {

                vectorArray[i] = (float) embedding.getDouble(String.valueOf(i));
            }
            return vectorArray;

        } catch (JSONException e) {
            throw new EmbedderException("Could not create the request body for OpenAI API: " + e.getMessage());
        }

    }
}
