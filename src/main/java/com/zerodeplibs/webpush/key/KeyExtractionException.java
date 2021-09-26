package com.zerodeplibs.webpush.key;

/**
 * Wraps checked exceptions such as {@link java.security.spec.InvalidKeySpecException}
 * that occur when extracting private/public keys.
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link KeyExtractionException#getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 */
public class KeyExtractionException extends RuntimeException {
    public KeyExtractionException(Throwable cause) {
        super(cause);
    }
}
