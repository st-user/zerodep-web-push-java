package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;

/**
 * <p>
 * This interface represents a signing key pair
 * for the Voluntary Application Server Identification (VAPID) described in <a href="https://datatracker.ietf.org/doc/html/rfc8292">RFC8292</a>.
 * </p>
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by using a factory method of {@link VAPIDKeyPairs}.
 * </p>
 *
 * <div><b>Example:</b></div>
 * <pre class="code">
 * VAPIDKeyPair vapidKeyPair = VAPIDKeyPairs.of(
 *      PrivateKeySources.ofPEMFile(new File(privateKeyFilePath).toPath()),
 *      PublicKeySources.ofPEMFile(new File(publicKeyFilePath).toPath())
 * );
 *
 * ........
 *
 * Request request = OkHttpClientRequestPreparer.getBuilder()
 *     .pushSubscription(subscription)
 *     .vapidJWTExpiresAfter(15, TimeUnit.MINUTES)
 *     .vapidJWTSubject("mailto:example@example.com")
 *     .pushMessage(message)
 *     .ttl(1, TimeUnit.HOURS)
 *     .urgencyLow()
 *     .topic("MyTopic")
 *     .build(vapidKeyPair)
 *     .toRequest();
 *
 * </pre>
 *
 * <p>
 * As explained in RFC8292, the key pair MUST be usable
 * with the Elliptic Curve Digital Signature Algorithm (ECDSA) over the P-256 curve.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Depends on implementations. See {@link VAPIDKeyPairs}.
 * </p>
 *
 * @author Tomoki Sato
 * @see PrivateKeySource
 * @see PublicKeySource
 * @see VAPIDKeyPairs
 * @see VAPIDJWTGenerator
 * @see VAPIDJWTParam
 */
public interface VAPIDKeyPair {

    /**
     * <p>
     * Extracts the octet sequence of the public key in uncompressed form
     * (65-byte array starting with 0x04).
     * </p>
     *
     * <p>
     * Typically, the extracted octet sequence is sent to browsers
     * and used to set the 'applicationServerKey' field.
     * </p>
     *
     * @return the octet sequence of the public key in uncompressed form.
     * @see PushSubscription
     */
    byte[] extractPublicKeyInUncompressedForm();

    /**
     * Generates a credential(that uses 'vapid' authentication scheme)
     * used to set an Authorization header field
     * when requesting the delivery of a push message.
     *
     * @param jwtParam parameters to use when generating JSON Web Token (JWT).
     * @return a credential like 'vapid t=eyJ0e....., k=BA1H....'.
     */
    String generateAuthorizationHeaderValue(VAPIDJWTParam jwtParam);
}
