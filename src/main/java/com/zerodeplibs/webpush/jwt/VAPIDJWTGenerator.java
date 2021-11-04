package com.zerodeplibs.webpush.jwt;

/**
 * <p>
 * The interface to a generator that generates JSON Web Token (JWT) used
 * for the Voluntary Application Server Identification
 * (<a href="https://datatracker.ietf.org/doc/html/rfc8292">VAPID</a>).
 * </p>
 *
 * <p>
 * If you have dependencies on one or more sub-modules for {@link VAPIDJWTGenerator},
 * the implementation can be provided by the sub-module(s).
 * </p>
 *
 * <p>
 * Of course, you can use arbitrary 3rd party libraries to implement this interface.
 * For example, if you want to use <a href="https://github.com/auth0/java-jwt">Auth0 Java JWT library</a>,
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
 * @author Tomoki Sato
 * @see VAPIDJWTGeneratorFactory
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
