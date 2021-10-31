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
 * Typically, an implementation of this interface is provided
 * by a sub-module for {@link VAPIDJWTGenerator}.
 * If you have dependencies on one or more of the sub-modules,
 * the instance can be loaded via {@link java.util.ServiceLoader}.
 * So, in this case, you don't have to instantiate {@link VAPIDJWTGeneratorFactory}
 * and {@link VAPIDJWTGenerator} directly.
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
