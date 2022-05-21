package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.jwt.DefaultVAPIDJWTGeneratorFactory;
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
 * <p>
 * Static factory methods used to create instances of {@link VAPIDKeyPair}.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <ul>
 * <li>
 * Instances obtained through {@link #of(PrivateKeySource, PublicKeySource, BiFunction)} are
 * thread-safe only when an instance obtained from <code>jwtGeneratorFactory</code> is thread-safe.
 * </li>
 * <li>
 * When you use {@link #of(PrivateKeySource, PublicKeySource)}
 * without sub-modules for {@link VAPIDJWTGenerator},
 * obtained instances are thread-safe.
 * Because the default implementation of {@link VAPIDJWTGenerator} is thread-safe.
 * </li>
 * <li>
 * When you use {@link #of(PrivateKeySource, PublicKeySource)} with a sub-module provided by <a href="https://github.com/st-user/zerodep-web-push-java-ext-jwt">zerodep-web-push-java-ext-jwt</a>,
 * obtained instances are thread-safe.
 * </li>
 * </ul>
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
     * <p>
     * Creates a new {@link VAPIDKeyPair} with the given private key source and
     * the given public key source.
     * </p>
     *
     * <p>
     * The implementation of {@link VAPIDJWTGenerator} can be provided
     * by one of the sub-modules
     * (<a href="https://github.com/st-user/zerodep-web-push-java-ext-jwt">zerodep-web-push-java-ext-jwt</a>)
     * on your classpath.
     * If such sub-modules don't exist, the default implementation
     * (created by {@link com.zerodeplibs.webpush.jwt.DefaultVAPIDJWTGeneratorFactory}) is used.
     * </p>
     *
     * @param privateKeySource a private key source.
     * @param publicKeySource  a public key source.
     * @return a new {@link VAPIDKeyPair}
     * @throws InvalidECPublicKeyException if the public key extracted
     *                                     from the given public key source is invalid.
     * @see com.zerodeplibs.webpush.jwt.DefaultVAPIDJWTGeneratorFactory
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
            return new DefaultVAPIDJWTGeneratorFactory();
        }
        return factoryIterator.next();
    }
}
