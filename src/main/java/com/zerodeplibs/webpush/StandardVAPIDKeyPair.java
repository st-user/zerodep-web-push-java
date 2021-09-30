package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PublicKeySource;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.BiFunction;

/**
 * A standard implementation of the VAPIDKeyPair.
 *
 * @author Tomoki Sato
 */
class StandardVAPIDKeyPair implements VAPIDKeyPair {

    private final byte[] uncompressedPublicKey;
    private final String uncompressedPublicKeyBase64;
    private final VAPIDJWTGenerator jwtGenerator;


    StandardVAPIDKeyPair(PrivateKeySource privateKeySource,
                         PublicKeySource publicKeySource,
                         BiFunction<ECPrivateKey, ECPublicKey, VAPIDJWTGenerator>
                             jwtGeneratorFactory) {

        WebPushPreConditions.checkNotNull(privateKeySource, "privateKeySource");
        WebPushPreConditions.checkNotNull(publicKeySource, "publicKeySource");
        WebPushPreConditions.checkNotNull(jwtGeneratorFactory, "the factory for VAPIDJWTGenerator");

        ECPrivateKey privateKey = privateKeySource.extract();
        ECPublicKey publicKey = publicKeySource.extract();

        this.uncompressedPublicKey = publicKeySource.extractUncompressedBytes();

        this.uncompressedPublicKeyBase64 = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(this.uncompressedPublicKey);

        this.jwtGenerator = jwtGeneratorFactory.apply(privateKey, publicKey);
    }

    @Override
    public byte[] extractUncompressedPublicKey() {
        return Arrays.copyOf(this.uncompressedPublicKey, this.uncompressedPublicKey.length);
    }

    @Override
    public String generateAuthorizationHeaderValue(VAPIDJWTParam jwtParam) {

        WebPushPreConditions.checkNotNull(jwtParam, "jwtParam");

        return String.format("vapid t=%s, k=%s", this.jwtGenerator.generate(jwtParam),
            this.uncompressedPublicKeyBase64);
    }

}
