package com.zerodeplibs.webpush;

import java.security.NoSuchAlgorithmException;

/**
 * Static factory methods for {@link MessageEncryption}.
 *
 * <p>
 * <b>Thread Safety:</b><br>
 * The factory methods themselves are thread-safe,
 * but the returned objects are <b>NOT</b> thread-safe.
 * </p>
 *
 * @author Tomoki Sato
 */
public class MessageEncryptions {

    private MessageEncryptions() {
    }

    /**
     * Creates a new {@link MessageEncryption}.
     *
     * @return a new {@link MessageEncryption}.
     */
    public static MessageEncryption of() {
        try {
            return new Aes128GcmMessageEncryption();
        } catch (NoSuchAlgorithmException e) {
            throw new MessageEncryptionException(e);
        }
    }
}
