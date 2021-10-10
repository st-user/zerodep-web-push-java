package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;
import java.util.Collections;

public class MyVertxVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final Vertx vertx;
    private final String pemPrivateKey;

    public MyVertxVAPIDJWTGenerator(Vertx vertx, ECPrivateKey privateKey) {
        this.vertx = vertx;
        this.pemPrivateKey = "-----BEGIN PRIVATE KEY-----\n" +
            Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n"
            + "-----END PRIVATE KEY-----\n";
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        JWTAuth provider = JWTAuth.create(
            vertx, new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setAlgorithm("ES256")
                        .setBuffer(pemPrivateKey)
                )
        );

        int expiresInSeconds =
            (int) (param.getExpiresAtInSeconds() - System.currentTimeMillis() / 1000);

        JWTOptions jwtOptions = new JWTOptions().setAlgorithm("ES256")
            .setAudience(Collections.singletonList(param.getOrigin()))
            .setExpiresInSeconds(expiresInSeconds);

        param.getSubject().ifPresent(jwtOptions::setSubject);

        JsonObject payload = new JsonObject();
        param.forEachAdditionalClaim(payload::put);

        return provider.generateToken(new JsonObject(payload.encode()), jwtOptions);
    }
}
