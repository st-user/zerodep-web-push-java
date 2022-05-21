package com.zerodeplibs.webpush.jwt;

import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * <p>
 * Implementations of this interface provide the functionality
 * for creating {@link VAPIDJWTGenerator}.
 * </p>
 *
 * <p>
 * Typically, an implementation of this interface is provided automatically.
 * </p>
 *
 * <ul>
 * <li>
 * If you don't have a dependency on a sub-module for {@link VAPIDJWTGenerator},
 * the default implementation({@link DefaultVAPIDJWTGeneratorFactory}) is automatically used.
 * </li>
 * <li>
 * If you have dependencies on one or more of the sub-modules(<a href="https://github.com/st-user/zerodep-web-push-java-ext-jwt">zerodep-web-push-java-ext-jwt</a>),
 * the instance of {@link VAPIDJWTGeneratorFactory}
 * can be loaded via {@link java.util.ServiceLoader}.
 * </li>
 * </ul>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Depends on implementations.
 * The default implementation({@link DefaultVAPIDJWTGeneratorFactory}) and
 * all of the implementations provided by <a href="https://github.com/st-user/zerodep-web-push-java-ext-jwt">zerodep-web-push-java-ext-jwt</a>
 * are thread-safe.
 * </p>
 *
 * @author Tomoki Sato
 * @see VAPIDKeyPairs#of(PrivateKeySource, PublicKeySource)
 */
@FunctionalInterface
public interface VAPIDJWTGeneratorFactory {

    /**
     * Creates a new {@link VAPIDJWTGenerator} with the given private key and public key.
     *
     * @param privateKey a private key.
     * @param publicKey  a public key.
     * @return a new {@link VAPIDJWTGenerator}.
     */
    VAPIDJWTGenerator create(ECPrivateKey privateKey, ECPublicKey publicKey);
}
