package one.cax.doc_search.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

@Component
public class JwtTokenProvider {

    private final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${auth_public_key}")
    private String publicKeyValue;

    private final JwtsWrapper jwtsWrapper;

    @Autowired
    public JwtTokenProvider(JwtsWrapper jwtsWrapper) {
        this.jwtsWrapper = jwtsWrapper;
    }

    /**
     * Validate the JWT token.
     * @param token The token to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validate(String token) {
        try {
            jwtsWrapper.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
                logger.severe("Expired JWT token");
                return false;
            } catch (SignatureException e) {
                logger.severe("Invalid JWT signature");
                return false;
            } catch (Exception e) {
                logger.severe("exception while validation token: " + e.getMessage());
                return false;
            }
    }

    /**
     * Get the public key from the configuration.
     * @return The public key.
     */
    private PublicKey getPublicKey() throws JwtTokenProviderException {
        try {

            byte[] byteKey = Base64.getDecoder().decode(publicKeyValue.getBytes());
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(x509EncodedKeySpec);

        } catch (Exception e) {
            logger.severe("Error while getting public key");
            throw new JwtTokenProviderException("Error while getting public key: " + e.getMessage(), e);
        }
    }

    public Authentication getAuthentication(String token) throws JwtTokenProviderException {
        // Parse the token and get the claims
        String username = null;
        try {

            var claims = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token);
            username = claims.getPayload().get("username", String.class);

        } catch (Exception e) {
            logger.severe("Error while parsing token");
            throw new JwtTokenProviderException("Error while parsing token: " + e.getMessage() , e);
        }


        // Return an Authentication object
        return new UsernamePasswordAuthenticationToken(username, null, null);
    }
}