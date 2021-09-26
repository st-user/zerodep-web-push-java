package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.BiFunction;

/**
 * This class represents a signing key pair
 * for the Voluntary Application Server Identification (VAPID) described in <a href="https://datatracker.ietf.org/doc/html/rfc8292">RFC8292</a>.
 *
 * <p>
 * <b>Example:</b>
 * <pre class="code">
 * VAPIDKeyPair vapidKeyPair = new VAPIDKeyPair(
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
 *
 * @see PrivateKeySource
 * @see PublicKeySource
 * @see VAPIDJWTGenerator
 * @see VAPIDJWTParam
 */
public class VAPIDKeyPair {

    private final byte[] uncompressedPublicKey;
    private final String uncompressedPublicKeyBase64;
    private final VAPIDJWTGenerator jwtGenerator;

    /**
     * Constructs a new VAPIDKeyPair with the private key source,
     * the public key source and the factory for {@link VAPIDJWTGenerator}.
     *
     * @param privateKeySource the private key source.
     * @param publicKeySource the public key source.
     * @param jwtGeneratorFactory the factory for {@link VAPIDJWTGenerator}.
     */
    public VAPIDKeyPair(PrivateKeySource privateKeySource, PublicKeySource publicKeySource,
                        BiFunction<ECPrivateKey, ECPublicKey, VAPIDJWTGenerator> jwtGeneratorFactory) {

        ECPrivateKey privateKey = privateKeySource.extract();
        ECPublicKey publicKey = publicKeySource.extract();

        this.uncompressedPublicKey = publicKeySource.extractUncompressedBytes();

        this.uncompressedPublicKeyBase64 = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(this.uncompressedPublicKey);

        this.jwtGenerator = jwtGeneratorFactory.apply(privateKey, publicKey);
    }

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
    public byte[] extractUncompressedPublicKeyBytes() {
        return Arrays.copyOf(this.uncompressedPublicKey, this.uncompressedPublicKey.length);
    }

    /**
     * Generates the value to set in the Authorization header field
     * when requesting the delivery of a push message.
     *
     * @param jwtParam the parameters to use when generating JSON Web Token (JWT).
     * @return the value to set in the Authorization header field.
     */
    public String generateAuthorizationHeaderValue(VAPIDJWTParam jwtParam) {
        return String.format("vapid t=%s, k=%s", this.jwtGenerator.generate(jwtParam),
            this.uncompressedPublicKeyBase64);
    }

}
