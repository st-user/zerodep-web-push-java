package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.ec.ECSigner;
import java.security.interfaces.ECPrivateKey;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.slf4j.LoggerFactory;

/**
 * The example implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://github.com/fusionauth/fusionauth-jwt">FusionAuth JWT</a>.
 */
public class MyFusionAuthJwtVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    public MyFusionAuthJwtVAPIDJWTGenerator(ECPrivateKey privateKey) {
        LoggerFactory.getLogger(getClass()).info("Using " + getClass().getSimpleName());
        this.privateKey = privateKey;
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        ECSigner signer = ECSigner.newSHA256Signer(privateKey);

        JWT jwt = new JWT()
            .setAudience(param.getOrigin())
            .setExpiration(ZonedDateTime.ofInstant(
                param.getExpiresAt().toInstant(),
                ZoneOffset.UTC
            ));

        param.getSubject().ifPresent(jwt::setSubject);
        param.forEachAdditionalClaim(jwt::addClaim);

        return JWT.getEncoder().encode(jwt, signer);
    }
}
