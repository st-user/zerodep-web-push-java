package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.key.PublicKeySource;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class represents the user agent-side keys for push message encryption.
 *
 * <p>
 * Typically, an instance of this class is created from a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field of a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>.
 * </p>
 *
 * @author Tomoki Sato
 * @see PushSubscription
 */
public class UserAgentMessageEncryptionKeys {

    private final ECPublicKey uaPublic;
    private final byte[] uncompressedUaPublic;
    private final byte[] authSecret;

    private UserAgentMessageEncryptionKeys(ECPublicKey uaPublic, byte[] uncompressedUaPublic,
                                           byte[] authSecret) {
        this.uaPublic = uaPublic;
        this.uncompressedUaPublic = uncompressedUaPublic;
        this.authSecret = authSecret;
    }

    /**
     * Creates a new UserAgentMessageEncryptionKeys from the PushSubscription.
     *
     * @param subscription a PushSubscription.
     * @return a new UserAgentMessageEncryptionKeys.
     */
    public static UserAgentMessageEncryptionKeys from(PushSubscription subscription) {
        return of(subscription.getKeys().getP256dh(), subscription.getKeys().getAuth());
    }

    /**
     * Creates a new UserAgentMessageEncryptionKeys
     * with the p256dh and the auth.
     *
     * <p>
     * It is assumed that the p256dh and the auth are base64-url encoded
     * (Typically, these are the values obtained from a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field of a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>).
     * </p>
     *
     * @param p256dh a p256dh.
     * @param auth   an auth.
     * @return a new UserAgentMessageEncryptionKeys.
     */
    public static UserAgentMessageEncryptionKeys of(String p256dh,
                                                    String auth) {
        return of(base64urlToBytes(p256dh), base64urlToBytes(auth));
    }

    /**
     * Creates a new UserAgentMessageEncryptionKeys
     * with the p256dh and the auth.
     *
     * <p>
     * This method is a byte array version
     * of {@link UserAgentMessageEncryptionKeys#of(String, String)}.
     * </p>
     *
     * @param p256dh a p256dh.
     * @param auth   an auth.
     * @return a new UserAgentMessageEncryptionKeys.
     */
    public static UserAgentMessageEncryptionKeys of(byte[] p256dh,
                                                    byte[] auth) {

        PublicKeySource publicKeySource =
            PublicKeySources.ofUncompressedBytes(p256dh);

        return new UserAgentMessageEncryptionKeys(
            publicKeySource.extract(),
            Arrays.copyOf(p256dh, p256dh.length),
            Arrays.copyOf(auth, auth.length));
    }

    private static byte[] base64urlToBytes(String text) {
        return Base64.getUrlDecoder().decode(text);
    }

    /**
     * Gets the ECPublicKey.
     * The public key is typically extracted from the p256dh field.
     *
     * @return the ECPublicKey.
     */
    public ECPublicKey getPublicKey() {
        return this.uaPublic;
    }

    /**
     * Gets the public key encoded in the uncompressed form[X9.62].
     * The public key is typically extracted from the p256dh field.
     *
     * @return the public key encoded in the uncompressed form.
     */
    public byte[] getUncompressedUaPublic() {
        return Arrays.copyOf(uncompressedUaPublic, uncompressedUaPublic.length);
    }

    /**
     * Gets the authSecret.
     * The authSecret is typically extracted from the auth field.
     *
     * @return the authSecret.
     */
    public byte[] getAuthSecret() {
        return Arrays.copyOf(authSecret, authSecret.length);
    }
}
