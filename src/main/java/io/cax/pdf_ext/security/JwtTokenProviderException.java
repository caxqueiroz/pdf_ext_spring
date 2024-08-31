package io.cax.pdf_ext.security;

public class JwtTokenProviderException extends Exception {
    public JwtTokenProviderException(String message) {
        super(message);
    }

    public JwtTokenProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtTokenProviderException(Throwable cause) {
        super(cause);
    }

}
