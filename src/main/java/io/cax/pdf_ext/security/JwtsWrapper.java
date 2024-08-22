package io.cax.pdf_ext.security;

import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import org.springframework.stereotype.Component;

@Component
public class JwtsWrapper {

    public JwtParserBuilder parser() {
        return Jwts.parser();
    }
}