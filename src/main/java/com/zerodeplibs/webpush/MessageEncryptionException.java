package com.zerodeplibs.webpush;

import java.security.InvalidAlgorithmParameterException;

/**
 * Wraps checked exceptions during message encryption
 * such as {@link InvalidAlgorithmParameterException}.
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link #getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 */
public class MessageEncryptionException extends RuntimeException {
    MessageEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    static MessageEncryptionException withDefaultMessage(Throwable cause) {
        return new MessageEncryptionException(
            "An exception was thrown during a cryptographic operation. Please check the cause.",
            cause);
    }
}
