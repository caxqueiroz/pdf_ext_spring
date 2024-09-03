package one.cax.doc_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Main application class for the Document Text Extraction and Search application.
 * This class serves as the entry point for the Spring Boot application.
 * It initializes the Spring context and starts the application.
 * 
 * The application is configured to exclude the UserDetailsServiceAutoConfiguration,
 * which is typically used for default user authentication. This exclusion suggests
 * that the application implements custom security configurations.
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class DocTextExtAndSearchApp {

    
    public static void main(String[] args) {
        SpringApplication.run(DocTextExtAndSearchApp.class, args);
    }

}
