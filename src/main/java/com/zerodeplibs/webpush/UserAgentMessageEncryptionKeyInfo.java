package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.MalformedUncompressedBytesException;
import com.zerodeplibs.webpush.key.PublicKeySource;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * <p>
 * This class represents keys of a user agent used to encrypting push messages.
 * </p>
 *
 * <p>
 * Typically, an instance of this class is created from a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field of a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>.
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
     * <p>
     * Creates a new {@link UserAgentMessageEncryptionKeyInfo}
     * with the given p256dh and auth.
     * </p>
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
     * <p>
     * Creates a new {@link UserAgentMessageEncryptionKeyInfo}
     * with the given p256dh and the auth.
     * </p>
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


    /**
     * Compares the given object with this object based on their public keys and secrets.
     *
     * @param o an object.
     * @return true if the given object is equal to this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAgentMessageEncryptionKeyInfo)) {
            return false;
        }
        UserAgentMessageEncryptionKeyInfo that = (UserAgentMessageEncryptionKeyInfo) o;
        return uaPublic.equals(that.uaPublic)
            && Arrays.equals(getUncompressedUaPublic(), that.getUncompressedUaPublic())
            && Arrays.equals(getAuthSecret(), that.getAuthSecret());
    }

    /**
     * Returns the hash code value for this object based on its public key and secret.
     *
     * @return the hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(uaPublic);
        result = 31 * result + Arrays.hashCode(getUncompressedUaPublic());
        result = 31 * result + Arrays.hashCode(getAuthSecret());
        return result;
    }

    @Override
    public String toString() {
        return "UserAgentMessageEncryptionKeyInfo{"
            + "p256dh='" + encodeBase64(uncompressedUaPublic) + '\''
            + ", auth='" + encodeBase64(authSecret)
            + "'}";
    }

    private String encodeBase64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
