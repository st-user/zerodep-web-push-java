package com.zerodeplibs.webpush.jwt;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Date;


/**
 * The default factory class for {@link VAPIDJWTGenerator}.
 *
 * <p>
 * If you want to specify an <a href="https://datatracker.ietf.org/doc/html/rfc8292#section-2.2">additional claim</a>,
 * its value must be an instance of {@link String}, {@link Boolean},
 * {@link Integer}, {@link Long}, {@link Double}, {@link Date} or {@link Instant}.
 * </p>
 *
 * @see com.zerodeplibs.webpush.VAPIDKeyPairs
 * @author Tomoki Sato
 */
public class DefaultVAPIDJWTGeneratorFactory implements VAPIDJWTGeneratorFactory {

    /**
     * Creates a new {@link VAPIDJWTGenerator} with the given private key and public key.
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
