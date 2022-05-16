package com.zerodeplibs.webpush.jwt;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Date;


/**
 * The default factory class for {@link VAPIDJWTGenerator}.
 *
 * @see com.zerodeplibs.webpush.VAPIDKeyPairs
 * @author Tomoki Sato
 */
public class DefaultVAPIDJWTGeneratorFactory implements VAPIDJWTGeneratorFactory {

    /**
     * <p>
     * Creates a new {@link VAPIDJWTGenerator} with the given private key and public key.
     * </p>
     *
     * <p>
     * When a parameter passed
     * to {@link VAPIDJWTGenerator#generate(VAPIDJWTParam)} of the returned generator
     * has additional claims, these values
     * must be an instance of {@link String}, {@link Boolean},
     * {@link Integer}, {@link Long}, {@link Double}, {@link Date} or {@link Instant}.
     * </p>
     *
     * @param privateKey a private key.
     * @param publicKey  a public key.
     * @return a new {@link VAPIDJWTGenerator}.
     */
    @Override
    public VAPIDJWTGenerator create(ECPrivateKey privateKey, ECPublicKey publicKey) {
        WebPushPreConditions.checkNotNull(privateKey, "privateKey");
        WebPushPreConditions.checkNotNull(publicKey, "publicKey");
        return new DefaultVAPIDJWTGenerator(privateKey);
    }
}
