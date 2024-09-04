package one.cax.doc_search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig is responsible for configuring Swagger documentation for the API.
 * 
 * This configuration class sets up the OpenAPI specification and groups the API endpoints.
 * It provides customization for the API information such as title, version, and description.
 * 
 * Key components:
 * - CustomOpenAPI: Defines the overall API information
 * - GroupedOpenApi: Configures the API grouping and path matching
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Document Text Extraction and Vector Search Store and API")
                        .version("1.0")
                        .description("API for extracting text from PDF documents and performing vector searches"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("doc-text-ext-and-search")
                .pathsToMatch("/**")
                .build();
    }
}