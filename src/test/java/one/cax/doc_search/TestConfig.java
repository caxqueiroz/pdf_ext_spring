package one.cax.doc_search;

import one.cax.doc_search.service.OpenAIEmbedderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@TestConfiguration
public class TestConfig {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    @Bean
    public OpenAIEmbedderService openAIEmbedderService() {
        System.out.println(apiKey);
        return new OpenAIEmbedderService(apiUrl, apiKey, model);
    }
}
