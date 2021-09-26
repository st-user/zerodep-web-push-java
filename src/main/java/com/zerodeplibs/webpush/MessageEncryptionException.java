package com.zerodeplibs.webpush;

import java.security.InvalidAlgorithmParameterException;

/**
 * Wraps checked exceptions during message encryption,
 * such as {@link InvalidAlgorithmParameterException}.
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link MessageEncryptionException#getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 */
public class MessageEncryptionException extends RuntimeException {
    MessageEncryptionException(Throwable cause) {
        super(cause);
    }
}
