package com.zerodeplibs.webpush;

import java.security.NoSuchAlgorithmException;

/**
 * Static utility methods for instantiating an implementation class of {@link MessageEncryption}.
 *
 * @author Tomoki Sato
 */
public class MessageEncryptions {


    /**
     * Creates a new MessageEncryption.
     *
     * @return s new MessageEncryption.
     */
    public static MessageEncryption of() {
        try {
            return new Aes128GcmMessageEncryption();
        } catch (NoSuchAlgorithmException e) {
            throw new MessageEncryptionException(e);
        }
    }
}
