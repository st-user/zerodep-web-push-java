package com.zerodeplibs.webpush.ext.jwt.jjwt;


import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import java.security.interfaces.ECPrivateKey;

/**
 * An implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://github.com/jwtk/jjwt">Java JWT</a>.
 *
 * @author Tomoki Sato
 * @see JavaJwtVAPIDJWTGeneratorFactory
 */
class JavaJwtVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    JavaJwtVAPIDJWTGenerator(ECPrivateKey privateKey) {
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
