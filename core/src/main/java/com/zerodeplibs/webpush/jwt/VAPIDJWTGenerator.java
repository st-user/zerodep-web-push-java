package com.zerodeplibs.webpush.jwt;

import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;

/**
 * <p>
 * The interface to a generator that generates JSON Web Token (JWT) used
 * for the Voluntary Application Server Identification
 * (<a href="https://datatracker.ietf.org/doc/html/rfc8292">VAPID</a>).
 * </p>
 *
 * <p>
 * Typically, you don't have to implement this interface by yourself.
 * When you use {@link VAPIDKeyPairs#of(PrivateKeySource, PublicKeySource)}
 * without any sub-module for this interface, the default implementation is automatically provided
 * through {@link DefaultVAPIDJWTGeneratorFactory}.
 * </p>
 *
 * <p>
 * If you have dependencies on one or more sub-modules for {@link VAPIDJWTGenerator}(<a href="https://github.com/st-user/zerodep-web-push-java/tree/main-v1/ext-jwt">zerodep-web-push-java-ext-jwt</a>),
 * the implementation can be provided by the sub-module(s).
 * </p>
 *
 * <p>
 * Of course, you can use arbitrary 3rd party libraries to make your own implementation.
 * For example, if you want to make your own implementation by utilizing <a href="https://github.com/auth0/java-jwt">Auth0 Java JWT library</a>,
 * The implementation will be something like below.
 * </p>
 * <pre class="code">
 *     class MyAuth0VAPIDJWTGenerator implements VAPIDJWTGenerator {
 *
 *         private final Algorithm jwtAlgorithm;
 *
 *         MyAuth0VAPIDJWTGenerator(ECPrivateKey privateKey, ECPublicKey publicKey) {
 *             this.jwtAlgorithm = Algorithm.ECDSA256(publicKey, privateKey);
 *         }
 *
 *         public String generate(VAPIDJWTParam param) {
 *             return JWT.create()
 *                 .withAudience(param.getOrigin())
 *                 .withExpiresAt(param.getExpiresAt())
 *                 .withSubject(param.getSubject().orElse("mailto:example@example.com"))
 *                 .sign(this.jwtAlgorithm);
 *         }
 *     }
 * </pre>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Depends on implementations.
 * The implementation provided through {@link DefaultVAPIDJWTGeneratorFactory} is thread-safe.
 * Most of the implementations provided by <a href="https://github.com/st-user/zerodep-web-push-java/tree/main-v1/ext-jwt">zerodep-web-push-java-ext-jwt</a>
 * are thread-safe. For more information, see its README.
 * </p>
 *
 * @author Tomoki Sato
 * @see VAPIDJWTGeneratorFactory
 * @see DefaultVAPIDJWTGeneratorFactory
 */
public interface VAPIDJWTGenerator {

    /**
     * Generates a JWT used for VAPID.
     *
     * @param param parameters used to build the JWT.
     * @return a JWT.
     */
    String generate(VAPIDJWTParam param);
}
