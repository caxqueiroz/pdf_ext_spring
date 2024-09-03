package one.cax.doc_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class})
public class DocTextExtAndSearchApp {

    public static void main(String[] args) {
        SpringApplication.run(DocTextExtAndSearchApp.class, args);
    }

}
