package com.zerodeplibs.webpush.ext.jwt.nimbusjose;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.zerodeplibs.webpush.jwt.VAPIDJWTCreationException;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.interfaces.ECPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://connect2id.com/products/nimbus-jose-jwt">Nimbus JOSE + JWT</a>.
 *
 * @author Tomoki Sato
 * @see NimbusJoseVAPIDJWTGeneratorFactory
 */
class NimbusJoseVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECDSASigner signer;
    private final JWSHeader header;

    NimbusJoseVAPIDJWTGenerator(ECPrivateKey privateKey) {
        try {
            this.signer = new ECDSASigner(privateKey);
        } catch (JOSEException e) {
            throw VAPIDJWTCreationException.withDefaultMessage(e);
        }
        this.header = new JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType.JWT)
            .build();
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("aud", param.getOrigin());
        payloadMap.put("exp", param.getExpiresAtInSeconds());

        param.getSubject().ifPresent(sub -> payloadMap.put("sub", sub));
        param.forEachAdditionalClaim(payloadMap::put);

        JWSObject jwsObject = new JWSObject(this.header, new Payload(payloadMap));
        try {
            jwsObject.sign(this.signer);
        } catch (JOSEException e) {
            throw VAPIDJWTCreationException.withDefaultMessage(e);
        }

        return jwsObject.serialize();
    }
}
