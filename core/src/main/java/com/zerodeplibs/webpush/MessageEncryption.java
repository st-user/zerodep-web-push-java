package com.zerodeplibs.webpush;

/**
 * Implementations of this interface provide the functionality of <a href="https://datatracker.ietf.org/doc/html/rfc8291">Message Encryption for Web Push</a>.
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by using a factory method of {@link MessageEncryptions}.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 *
 * <p>
 * Depends on implementations.
 * Typically, an implementation of this interface is not thread-safe.
 * See {@link MessageEncryptions}.
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
