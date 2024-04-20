package io.cax.pdf_ext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class})
public class PdfExtApp {

    public static void main(String[] args) {
        SpringApplication.run(PdfExtApp.class, args);
    }

}
