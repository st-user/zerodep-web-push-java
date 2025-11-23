package com.zerodeplibs.webpush.ext.jwt.auth0;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * An implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://github.com/auth0/java-jwt">Java JWT - auth0</a>.
 *
 * @author Tomoki Sato
 * @see Auth0VAPIDJWTGeneratorFactory
 */
class Auth0VAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final Algorithm jwtAlgorithm;

    Auth0VAPIDJWTGenerator(ECPrivateKey privateKey, ECPublicKey publicKey) {
        this.jwtAlgorithm = Algorithm.ECDSA256(publicKey, privateKey);
    }

    @Override
    public String generate(VAPIDJWTParam vapidjwtParam) {

        JWTCreator.Builder builder = JWT.create()
            .withAudience(vapidjwtParam.getOrigin())
            .withExpiresAt(vapidjwtParam.getExpiresAt());

        vapidjwtParam.getSubject().ifPresent(builder::withSubject);
        builder.withPayload(vapidjwtParam.getAdditionalClaims());

        return builder.sign(this.jwtAlgorithm);
    }
}
