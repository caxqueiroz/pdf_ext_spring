package io.cax.pdf_ext.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

@Component
public class JwtTokenProvider {

    private final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${auth.public_key}")
    private String publicKeyValue;

    /**
     * Validate the JWT token.
     * @param token The token to validate.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validate(String token) {
        try {
            Jwts.parser()
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
                logger.severe(e.getMessage());
                return false;
            }
    }

    /**
     * Get the public key from the configuration.
     * @return The public key.
     */
    private PublicKey getPublicKey() throws InvalidKeySpecException {
        try {

            byte[] byteKey = Base64.getDecoder().decode(publicKeyValue.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(X509publicKey);

        } catch(InvalidKeySpecException e){
            logger.severe(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Authentication getAuthentication(String token) {
        // Parse the token and get the claims
        String username = null;
        try {
            username = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload().getSubject();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }


        // Return an Authentication object
        return new UsernamePasswordAuthenticationToken(username, null, null);
    }
}