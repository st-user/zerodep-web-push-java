package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.function.BiFunction;

/**
 * Static utility methods for instantiating an implementation class of {@link VAPIDKeyPair}.
 *
 * @author Tomoki Sato
 */
public class VAPIDKeyPairs {

    /**
     * Creates a new VAPIDKeyPair with the private key source,
     * the public key source and the factory for {@link VAPIDJWTGenerator}.
     *
     * @param privateKeySource    the private key source.
     * @param publicKeySource     the public key source.
     * @param jwtGeneratorFactory the factory for {@link VAPIDJWTGenerator}.
     * @return a new VAPIDKeyPair
     * @throws InvalidECPublicKeyException if the public key extracted
     *                                     from the public key source is invalid.
     */
    public static VAPIDKeyPair of(PrivateKeySource privateKeySource,
                                  PublicKeySource publicKeySource,
                                  BiFunction<ECPrivateKey, ECPublicKey, VAPIDJWTGenerator>
                                      jwtGeneratorFactory) {
        return new StandardVAPIDKeyPair(privateKeySource, publicKeySource, jwtGeneratorFactory);
    }
}
