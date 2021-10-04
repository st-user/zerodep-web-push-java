package org.example;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.interfaces.ECPrivateKey;
import org.slf4j.LoggerFactory;

/**
 * The example implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://connect2id.com/products/nimbus-jose-jwt">Nimbus JOSE + JWT</a>.
 */
public class MyNimbusJoseJwtVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECDSASigner signer;
    private final JWSHeader header;

    public MyNimbusJoseJwtVAPIDJWTGenerator(ECPrivateKey privateKey) {
        LoggerFactory.getLogger(MyNimbusJoseJwtVAPIDJWTGenerator.class)
            .info("Using " + MyNimbusJoseJwtVAPIDJWTGenerator.class.getSimpleName());
        try {
            this.signer = new ECDSASigner(privateKey);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        this.header = new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType.JWT)
            .build();
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        String payload = String.format(
            "{\"aud\":\"%s\",\"exp\":%d,\"sub\":\"%s\"}",
            param.getOrigin(),
            param.getExpiresAtInSeconds(),
            param.getSubject().orElse("mailto:example@example.com")
        );

        JWSObject jwsObject = new JWSObject(this.header, new Payload(payload));
        try {
            jwsObject.sign(this.signer);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        return jwsObject.serialize();
    }
}
