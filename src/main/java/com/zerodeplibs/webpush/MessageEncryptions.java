package com.zerodeplibs.webpush;

import java.security.NoSuchAlgorithmException;

/**
 * Static utility methods for instantiating an implementation class of {@link MessageEncryption}.
 *
 * @author Tomoki Sato
 */
public class MessageEncryptions {


    /**
     * Creates an instance of the {@link MessageEncryption} implementation.
     *
     * @return an instance of the {@link MessageEncryption} implementation.
     */
    public static MessageEncryption of() {
        try {
            return new Aes128GcmMessageEncryption();
        } catch (NoSuchAlgorithmException e) {
            throw new WebPushRuntimeWrapperException(e);
        }
    }
}
