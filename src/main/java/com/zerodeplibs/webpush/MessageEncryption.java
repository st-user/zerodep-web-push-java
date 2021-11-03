package com.zerodeplibs.webpush;

/**
 * <p>
 * Implementations of this interface provide the functionality of <a href="https://datatracker.ietf.org/doc/html/rfc8291">Message Encryption for Web Push</a>.
 * </p>
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by using a factory method of {@link MessageEncryptions}.
 * </p>
 *
 * @author Tomoki Sato
 * @see MessageEncryptions
 */
public interface MessageEncryption {

    /**
     * Encrypts the given <code>pushMessage</code>
     * with the given <code>userAgentMessageEncryptionKeyInfo</code>.
     *
     * @param userAgentMessageEncryptionKeyInfo user agent side keys for encryption.
     * @param pushMessage                       a push message to be encrypted
     * @return the encrypted push message.
     */
    EncryptedPushMessage encrypt(
        UserAgentMessageEncryptionKeyInfo userAgentMessageEncryptionKeyInfo,
        PushMessage pushMessage);

}
