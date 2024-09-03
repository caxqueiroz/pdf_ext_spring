package one.cax.doc_search.config;

import one.cax.doc_search.service.OpenAIEmbedderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    @Bean
    public OpenAIEmbedderService openAIEmbedderService() {
        return new OpenAIEmbedderService(apiUrl, apiKey, model);
    }
}