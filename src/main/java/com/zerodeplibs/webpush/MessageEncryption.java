package com.zerodeplibs.webpush;

/**
 * Implementations of this interface provide the functionality of <a href="https://datatracker.ietf.org/doc/html/rfc8291">Message Encryption for Web Push</a>.
 *
 * <p>
 * Usually, an instance of the implementation class of this interface
 * is obtained by the factory method of {@link MessageEncryptions}.
 * </p>
 *
 * @author Tomoki Sato
 *
 * @see MessageEncryptions
 */
public interface MessageEncryption {

    /**
     * Encrypts the given pushMessage with the given subscriptionKeys.
     *
     * @param userAgentMessageEncryptionKeys push subscription keys for the user agent
     *                         to which the push message is sent.
     * @param pushMessage the push message to be encrypted.
     * @return the encrypted push message.
     */
    EncryptedPushMessage encrypt(UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys, PushMessage pushMessage);

}
