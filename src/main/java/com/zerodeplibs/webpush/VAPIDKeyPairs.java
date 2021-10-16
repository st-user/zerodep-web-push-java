package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGeneratorFactory;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

/**
 * Static factory methods for {@link VAPIDKeyPair}.
 *
 * <h3>Thread Safety:</h3>
 * <p>
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
        WebPushPreConditions.checkNotNull(jwtGeneratorFactory, "jwtGeneratorFactory");
        return new StandardVAPIDKeyPair(privateKeySource, publicKeySource,
            jwtGeneratorFactory::apply);
    }

    /**
     * Creates a new {@link VAPIDKeyPair} with the given private key source and
     * the given public key source.
     *
     * <p>
     * The implementation of {@link VAPIDJWTGenerator} is provided
     * by one of the sub-modules for {@link VAPIDJWTGenerator} on your classpath.
     * If such sub-modules don't exist, an {@link IllegalStateException} is thrown.
     * </p>
     *
     * @param privateKeySource a private key source.
     * @param publicKeySource  a public key source.
     * @return a new {@link VAPIDKeyPair}
     * @throws InvalidECPublicKeyException if the public key extracted
     *                                     from the given public key source is invalid.
     * @throws IllegalStateException       if no sub-module for {@link VAPIDJWTGenerator} exists.
     */
    public static VAPIDKeyPair of(PrivateKeySource privateKeySource,
                                  PublicKeySource publicKeySource) {
        return new StandardVAPIDKeyPair(
            privateKeySource,
            publicKeySource,
            loadVAPIDJWTGeneratorFactory());
    }

    private static VAPIDJWTGeneratorFactory loadVAPIDJWTGeneratorFactory() {
        ServiceLoader<VAPIDJWTGeneratorFactory> loader =
            ServiceLoader.load(VAPIDJWTGeneratorFactory.class,
                VAPIDJWTGeneratorFactory.class.getClassLoader());
        Iterator<VAPIDJWTGeneratorFactory> factoryIterator = loader.iterator();
        if (!factoryIterator.hasNext()) {
            throw new IllegalStateException("No sub-module for VAPIDJWTGenerator exists.");
        }
        return factoryIterator.next();
    }
}
