package com.zerodeplibs.webpush;

/**
 * Implementations of this interface provide the functionality of <a href="https://datatracker.ietf.org/doc/html/rfc8291">Message Encryption for Web Push</a>.
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by a factory method of {@link MessageEncryptions}.
 * </p>
 *
 * @author Tomoki Sato
 * @see MessageEncryptions
 */
public interface MessageEncryption {

    /**
     * Encrypts the given pushMessage with the given userAgentMessageEncryptionKeyInfo.
     *
     * @param userAgentMessageEncryptionKeyInfo user agent side keys for encryption.
     * @param pushMessage                       a push message to be encrypted
     * @return the encrypted push message.
     */
    EncryptedPushMessage encrypt(
        UserAgentMessageEncryptionKeyInfo userAgentMessageEncryptionKeyInfo,
        PushMessage pushMessage);

}
