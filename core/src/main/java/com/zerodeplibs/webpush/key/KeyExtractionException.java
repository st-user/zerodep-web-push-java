package com.zerodeplibs.webpush.key;

/**
 * <p>
 * Wraps checked exceptions such as {@link java.security.spec.InvalidKeySpecException}
 * that may occur when a private/public key is being extracted.
 * </p>
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link KeyExtractionException#getCause()}.
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
