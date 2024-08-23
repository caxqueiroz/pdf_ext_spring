package io.cax.pdf_ext.service;

import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbedderService {
    private static final VectorTypeSupport vectorTypeSupport = VectorizationProvider.getInstance().getVectorTypeSupport();

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    /**
     * Embed a text using OpenAI API
     * @param text
     * @return
     */
    public VectorFloat<?> embed(String text) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("input", text);
        requestBody.put("model", model);


        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, entity, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        JSONObject embedding = responseBody.getJSONArray("data").getJSONObject(0).getJSONObject("embedding");

        // Convert the embedding to VectorFloat
        float[] vectorArray = new float[embedding.length()];
        for (int i = 0; i < embedding.length(); i++) {
            vectorArray[i] = embedding.getFloat(String.valueOf(i));
        }
        return vectorTypeSupport.createFloatVector(vectorArray);

    }
}
