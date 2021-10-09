package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.security.interfaces.ECPrivateKey;
import org.slf4j.LoggerFactory;

/**
 * The example implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://github.com/jwtk/jjwt">Java JWT</a>.
 */
public class MyJJwtVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    public MyJJwtVAPIDJWTGenerator(ECPrivateKey privateKey) {
        LoggerFactory.getLogger(getClass()).info("Using " + getClass().getSimpleName());
        this.privateKey = privateKey;
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        JwtBuilder jwtBuilder = Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setHeaderParam("alg", "ES256")
            .setAudience(param.getOrigin())
            .setExpiration(param.getExpiresAt())
            .signWith(privateKey);

        param.getSubject().ifPresent(jwtBuilder::setSubject);
        param.forEachAdditionalClaim(jwtBuilder::claim);

        return jwtBuilder.compact();
    }
}
