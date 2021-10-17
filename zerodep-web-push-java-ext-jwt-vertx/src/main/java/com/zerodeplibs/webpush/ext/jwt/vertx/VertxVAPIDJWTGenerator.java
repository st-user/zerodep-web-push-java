package com.zerodeplibs.webpush.ext.jwt.vertx;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.lang.reflect.Method;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * An implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://vertx.io/docs/vertx-auth-jwt/java/">JWT Auth - Vert.x</a>.
 *
 * @author Tomoki Sato
 * @see VertxVAPIDJWTGeneratorFactory
 */
public class VertxVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private static final boolean IS_SET_BUFFER_PRESENT;

    static {
        boolean isSetBufferExists = false;

        try {
            Method setBuffer = PubSecKeyOptions.class.getMethod("setBuffer", String.class);
            if (PubSecKeyOptions.class.isAssignableFrom(setBuffer.getReturnType())) {
                isSetBufferExists = true;
            }
        } catch (NoSuchMethodException e) {
            // Ignore.
        }

        IS_SET_BUFFER_PRESENT = isSetBufferExists;
    }

    private final Supplier<Vertx> vertxObtainStrategy;
    private final String privateKey;

    VertxVAPIDJWTGenerator(Supplier<Vertx> vertxObtainStrategy, ECPrivateKey privateKey) {
        if (vertxObtainStrategy == null) {
            throw new NullPointerException("vertxObtainStrategy should not be null.");
        }
        this.vertxObtainStrategy = vertxObtainStrategy;
        if (IS_SET_BUFFER_PRESENT) {
            this.privateKey = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n"
                + "-----END PRIVATE KEY-----\n";
        } else {
            this.privateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        JWTAuth provider = JWTAuth.create(
            this.vertxObtainStrategy.get(), new JWTAuthOptions()
                .addPubSecKey(
                    createOptions()
                )
        );

        int expiresInSeconds =
            (int) (param.getExpiresAtInSeconds() - System.currentTimeMillis() / 1000);

        JWTOptions jwtOptions = new JWTOptions().setAlgorithm("ES256")
            .setAudience(Collections.singletonList(param.getOrigin()))
            .setExpiresInSeconds(expiresInSeconds);

        param.getSubject().ifPresent(jwtOptions::setSubject);

        JsonObject payload = new JsonObject();
        if (!param.getAdditionalClaims().isEmpty()) {
            payload = JsonObject.mapFrom(param.getAdditionalClaims());
        }

        return provider.generateToken(payload, jwtOptions);
    }

    @SuppressWarnings("deprecation")
    private PubSecKeyOptions createOptions() {
        if (IS_SET_BUFFER_PRESENT) {
            return Vertx4Support.createOptions(this.privateKey);
        }
        return new PubSecKeyOptions()
            .setAlgorithm("ES256")
            // TODO From Vert.x v4, we should use #setBuffer
            .setSecretKey(this.privateKey);
    }

}
