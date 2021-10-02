package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.MalformedUncompressedBytesException;
import com.zerodeplibs.webpush.key.PublicKeySource;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class represents a user agent side keys for push message encryption.
 *
 * <p>
 * Typically, an instance of this class is created by using a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field of a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>.
 * </p>
 *
 * @author Tomoki Sato
 * @see PushSubscription
 */
public class UserAgentMessageEncryptionKeyInfo {

    private final ECPublicKey uaPublic;
    private final byte[] uncompressedUaPublic;
    private final byte[] authSecret;

    private UserAgentMessageEncryptionKeyInfo(ECPublicKey uaPublic, byte[] uncompressedUaPublic,
                                              byte[] authSecret) {
        this.uaPublic = uaPublic;
        this.uncompressedUaPublic = uncompressedUaPublic;
        this.authSecret = authSecret;
    }

    /**
     * Creates a new {@link UserAgentMessageEncryptionKeyInfo}
     * from a {@link PushSubscription}'s 'keys' field.
     *
     * @param subscriptionKeys a PushSubscription's 'keys' field.
     * @return a new {@link UserAgentMessageEncryptionKeyInfo}.
     * @throws IllegalArgumentException            if the 'keys.p256dh' is invalid
     *                                             as a base64url string or the 'keys.auth'
     *                                             is invalid as a base64url string.
     * @throws MalformedUncompressedBytesException if the given p256dh doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     * @throws InvalidECPublicKeyException         if the public key extracted
     *                                             from the give p256dh is invalid.
     * @see PushSubscription
     */
    public static UserAgentMessageEncryptionKeyInfo from(PushSubscription.Keys subscriptionKeys) {

        WebPushPreConditions.checkNotNull(subscriptionKeys, "subscriptionKeys");

        return of(subscriptionKeys.getP256dh(), subscriptionKeys.getAuth());
    }

    /**
     * Creates a new {@link UserAgentMessageEncryptionKeyInfo}
     * with the given p256dh and auth.
     *
     * <p>
     * It is assumed that the p256dh and the auth are base64-url encoded
     * (Typically, these are the values obtained from a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field of a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>).
     * </p>
     *
     * @param p256dh a p256dh.
     * @param auth   an auth.
     * @return a new {@link UserAgentMessageEncryptionKeyInfo}.
     * @throws IllegalArgumentException            if the 'keys.p256dh' is invalid
     *                                             as a base64url string or the 'keys.auth'
     *                                             is invalid as a base64url string.
     * @throws MalformedUncompressedBytesException if the given p256dh doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     * @throws InvalidECPublicKeyException         if the public key extracted
     *                                             from the give p256dh is invalid.
     */
    public static UserAgentMessageEncryptionKeyInfo of(String p256dh,
                                                       String auth) {

        WebPushPreConditions.checkNotNull(p256dh, "p256dh");
        WebPushPreConditions.checkNotNull(auth, "auth");

        return of(base64urlToBytes(p256dh), base64urlToBytes(auth));
    }

    /**
     * Creates a new {@link UserAgentMessageEncryptionKeyInfo}
     * with the given p256dh and the auth.
     *
     * <p>
     * This method is a byte array version
     * of {@link UserAgentMessageEncryptionKeyInfo#of(String, String)}.
     * </p>
     *
     * @param p256dh a p256dh.
     * @param auth   an auth.
     * @return a new {@link UserAgentMessageEncryptionKeyInfo}.
     * @throws MalformedUncompressedBytesException if the given p256dh doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     * @throws InvalidECPublicKeyException         if the public key extracted
     *                                             from the give p256dh is invalid.
     */
    public static UserAgentMessageEncryptionKeyInfo of(byte[] p256dh,
                                                       byte[] auth) {

        WebPushPreConditions.checkNotNull(p256dh, "p256dh");
        WebPushPreConditions.checkNotNull(auth, "auth");

        PublicKeySource publicKeySource =
            PublicKeySources.ofUncompressedBytes(p256dh);

        return new UserAgentMessageEncryptionKeyInfo(
            publicKeySource.extract(),
            Arrays.copyOf(p256dh, p256dh.length),
            Arrays.copyOf(auth, auth.length));
    }

    private static byte[] base64urlToBytes(String text) {
        return Base64.getUrlDecoder().decode(text);
    }

    ECPublicKey getPublicKey() {
        return this.uaPublic;
    }

    byte[] getUncompressedUaPublic() {
        return Arrays.copyOf(uncompressedUaPublic, uncompressedUaPublic.length);
    }

    byte[] getAuthSecret() {
        return Arrays.copyOf(authSecret, authSecret.length);
    }
}
