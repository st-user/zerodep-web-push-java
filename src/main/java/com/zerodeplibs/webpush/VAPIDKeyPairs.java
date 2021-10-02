package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.function.BiFunction;

/**
 * Static factory methods for {@link VAPIDKeyPair}.
 *
 * <p>
 * <b>Thread Safety:</b><br>
 * The factory methods themselves are thread-safe.
 * The returned objects are thread-safe only if an instance
 * obtained from <code>jwtGeneratorFactory</code> is thread-safe.
 * </p>
 *
 * @author Tomoki Sato
 */
public class VAPIDKeyPairs {

    private VAPIDKeyPairs() {
    }

    /**
     * Creates a new {@link VAPIDKeyPair} with the given private key source,
     * the given public key source and the given factory for {@link VAPIDJWTGenerator}.
     *
     * @param privateKeySource    a private key source.
     * @param publicKeySource     a public key source.
     * @param jwtGeneratorFactory a factory for {@link VAPIDJWTGenerator}.
     * @return a new {@link VAPIDKeyPair}
     * @throws InvalidECPublicKeyException if the public key extracted
     *                                     from the given public key source is invalid.
     */
    public static VAPIDKeyPair of(PrivateKeySource privateKeySource,
                                  PublicKeySource publicKeySource,
                                  BiFunction<ECPrivateKey, ECPublicKey, VAPIDJWTGenerator>
                                      jwtGeneratorFactory) {
        return new StandardVAPIDKeyPair(privateKeySource, publicKeySource, jwtGeneratorFactory);
    }
}
