package one.cax.doc_search.security;

import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtsWrapper {

    public JwtParserBuilder parser() {
        return Jwts.parser();
    }
}