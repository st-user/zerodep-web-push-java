package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;

/**
 * This interface represents a signing key pair
 * for the Voluntary Application Server Identification (VAPID) described in <a href="https://datatracker.ietf.org/doc/html/rfc8292">RFC8292</a>.
 *
 * <p>
 * Usually, an instance of the implementation class of this interface
 * is obtained by the factory method of {@link VAPIDKeyPairs}.
 * </p>
 *
 * <p>
 * <b>Example:</b>
 * <pre class="code">
 * VAPIDKeyPair vapidKeyPair = VAPIDKeyPairs.of(
 *      PrivateKeySources.ofPEMFile(new File(privateKeyFilePath).toPath()),
 *      PublicKeySources.ofPEMFile(new File(publicKeyFilePath).toPath()),
 *      MyAuth0VAPIDJWTGenerator::new
 * );
 *
 * ........
 *
 * VAPIDJWTParam jwtParam = ......
 * String headerValue = vapidKeyPair.createAuthorizationHeaderValue(jwtParam);
 * myHeader.addHeader("Authorization", headerValue);
 *
 * </pre>
 * </p>
 *
 * <p>
 * As explained in RFC8292, the key pair MUST be usable
 * with the Elliptic Curve Digital Signature Algorithm (ECDSA) over the P-256 curve.
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
     * Extracts the byte array of the elliptic curve (EC) public key in uncompressed form
     * (a 65-byte array starting with 0x04).
     *
     * <p>
     * Typically, the return value of this method is sent to browsers
     * and used to set the 'applicationServerKey' fields when calling 'pushManager.subscribe()'.
     * </p>
     *
     * @return the byte array of the public key in uncompressed form.
     * @see PushSubscription
     */
    byte[] extractUncompressedPublicKey();

    /**
     * Generates the value to set in the Authorization header field
     * when requesting the delivery of a push message.
     *
     * @param jwtParam the parameters to use when generating JSON Web Token (JWT).
     * @return the value to set in the Authorization header field.
     */
    String generateAuthorizationHeaderValue(VAPIDJWTParam jwtParam);
}
