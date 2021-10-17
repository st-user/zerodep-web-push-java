package com.zerodeplibs.webpush.key;

/**
 * Wraps checked exceptions such as {@link java.security.spec.InvalidKeySpecException}
 * that occur when extracting private/public keys.
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link #getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 */
public class KeyExtractionException extends RuntimeException {
    KeyExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    static KeyExtractionException withDefaultMessage(Throwable cause) {
        return new KeyExtractionException(
            "An exception was thrown while extracting a key. Please check the cause.", cause);
    }
}
