package com.zerodeplibs.webpush.ext.jwt.fusionauth;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.ec.ECSigner;
import java.security.interfaces.ECPrivateKey;
import java.time.ZoneOffset;
import java.util.Base64;

/**
 * An implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://github.com/fusionauth/fusionauth-jwt">FusionAuth JWT</a>.
 *
 * @author Tomoki Sato
 * @see FusionAuthVAPIDJWTGeneratorFactory
 */
class FusionAuthVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final String privateKeyPem;

    FusionAuthVAPIDJWTGenerator(ECPrivateKey privateKey) {
        this.privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(privateKey.getEncoded()) + "\n"
            + "-----END PRIVATE KEY-----";
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        // TODO As of 5.0.0, we can pass an ECPrivateKey object directly.
        ECSigner signer = ECSigner.newSHA256Signer(privateKeyPem);

        JWT jwt = new JWT()
            .setAudience(param.getOrigin())
            .setExpiration(param.getExpirationTime().atZone(ZoneOffset.UTC));

        param.getSubject().ifPresent(jwt::setSubject);
        param.forEachAdditionalClaim(jwt::addClaim);

        return JWT.getEncoder().encode(jwt, signer);
    }
}
