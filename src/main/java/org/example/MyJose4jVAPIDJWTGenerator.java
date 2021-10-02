package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.interfaces.ECPrivateKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.slf4j.LoggerFactory;

public class MyJose4jVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    public MyJose4jVAPIDJWTGenerator(ECPrivateKey privateKey) {
        LoggerFactory.getLogger(MyJose4jVAPIDJWTGenerator.class)
            .info("Using " + MyJose4jVAPIDJWTGenerator.class.getSimpleName());
        this.privateKey = privateKey;
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        JsonWebSignature jws = new JsonWebSignature();

        jws.setHeader("typ", "JWT");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);

        String payload = String.format(
            "{\"aud\":\"%s\",\"exp\":%d,\"sub\":\"%s\"}",
            param.getOrigin(),
            param.getExpiresAt().getTime() / 1000,
            param.getSubject().orElse("mailto:example@example.com")
        );
        jws.setPayload(payload);
        jws.setKey(privateKey);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }
}
