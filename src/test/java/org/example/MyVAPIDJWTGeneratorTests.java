package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyVAPIDJWTGeneratorTests {


    @Test
    public void generateWithAuth0()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException {

        KeyPair keyPair = generateKeyPair();

        App.MyAuth0VAPIDJWTGenerator generator =
            new App.MyAuth0VAPIDJWTGenerator((ECPrivateKey) keyPair.getPrivate(),
                (ECPublicKey) keyPair.getPublic());

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(60)
            .subject("mailto:test@example.com")
            .build();

        String jwt = generator.generate(param);
        verifySign(jwt, keyPair.getPublic());
    }

    @Test
    public void generateWithJOSE4j()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException {

        KeyPair keyPair = generateKeyPair();
        MyJose4jVAPIDJWTGenerator generator =
            new MyJose4jVAPIDJWTGenerator((ECPrivateKey) keyPair.getPrivate());

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(60)
            .subject("mailto:test@example.com")
            .build();

        String jwt = generator.generate(param);
        verifySign(jwt, keyPair.getPublic());
    }

    @Test
    public void generateWithNimbusJoseJwt()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException {

        KeyPair keyPair = generateKeyPair();
        MyNimbusJoseJwtVAPIDJWTGenerator generator =
            new MyNimbusJoseJwtVAPIDJWTGenerator((ECPrivateKey) keyPair.getPrivate());

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(60)
            .subject("mailto:test@example.com")
            .build();

        String jwt = generator.generate(param);
        verifySign(jwt, keyPair.getPublic());
    }

    private void verifySign(String jwt, PublicKey publicKey) throws JoseException {

        JsonWebSignature verifier = new JsonWebSignature();
        verifier.setAlgorithmConstraints(new AlgorithmConstraints(
            AlgorithmConstraints.ConstraintType.PERMIT,
            AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));

        verifier.setCompactSerialization(jwt);
        verifier.setKey(publicKey);

        Assertions.assertTrue(verifier.verifySignature());
    }

    private KeyPair generateKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGeneragor = KeyPairGenerator.getInstance("EC");
        keyPairGeneragor.initialize(new ECGenParameterSpec("secp256r1"));
        return keyPairGeneragor.generateKeyPair();
    }
}
