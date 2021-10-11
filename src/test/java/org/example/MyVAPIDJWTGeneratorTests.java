package org.example;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;

public class MyVAPIDJWTGeneratorTests {

    @Test
    public void generate()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException,
        IOException {

        Vertx vertx = Vertx.vertx();
        KeyPair keyPair = generateKeyPair();

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(60)
            .subject("mailto:test@example.com")
            .additionalClaim("adClaim", new MyAdditionalClaim("hello"))
            .build();

        MyVertxVAPIDJWTGenerator generator =
            new MyVertxVAPIDJWTGenerator(vertx, (ECPrivateKey) keyPair.getPrivate());

        String jwt = generator.generate(param);
        verifySign(jwt, keyPair.getPublic());
        assertHeader(jwt);
        assertPayload(jwt);
    }


    private void assertHeader(String jwt) throws IOException {
        byte[] decoded = splitAndDecode(0, jwt);
        JsonObject jsonObject = new JsonObject(Buffer.buffer(decoded));
        TestingJWTHeader actual = jsonObject.mapTo(TestingJWTHeader.class);

        assertEquals("JWT", actual.getTyp());
        assertEquals("ES256", actual.getAlg());
    }

    private void assertPayload(String jwt) throws IOException {
        byte[] decoded = splitAndDecode(1, jwt);
        JsonObject jsonObject = new JsonObject(Buffer.buffer(decoded));

        assertEquals("https://example.com", jsonObject.getString("aud"));
        assertTrue(jsonObject.getInteger("exp") > System.currentTimeMillis() / 1000);
        assertEquals("mailto:test@example.com", jsonObject.getString("sub"));


        JsonObject additionalClaims = jsonObject.getJsonObject("adClaim");
        assertEquals("hello", additionalClaims.getString("myClaim"));
    }

    private void verifySign(String jwt, PublicKey publicKey) throws JoseException {

        JsonWebSignature verifier = new JsonWebSignature();
        verifier.setAlgorithmConstraints(new AlgorithmConstraints(
            AlgorithmConstraints.ConstraintType.PERMIT,
            AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));

        verifier.setCompactSerialization(jwt);
        verifier.setKey(publicKey);

        assertTrue(verifier.verifySignature());
    }

    private KeyPair generateKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGeneragor = KeyPairGenerator.getInstance("EC");
        keyPairGeneragor.initialize(new ECGenParameterSpec("secp256r1"));
        return keyPairGeneragor.generateKeyPair();
    }

    private byte[] splitAndDecode(int pos, String jwt) {
        String content = jwt.split("\\.")[pos];
        return Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8));
    }

    static class TestingJWTHeader {
        private String typ;
        private String alg;

        public String getTyp() {
            return typ;
        }

        public String getAlg() {
            return alg;
        }
    }

    static class MyAdditionalClaim {

        private String myClaim;

        public MyAdditionalClaim() {
        }

        public MyAdditionalClaim(String myClaim) {
            this.myClaim = myClaim;
        }

        public String getMyClaim() {
            return myClaim;
        }

        public void setMyClaim(String myClaim) {
            this.myClaim = myClaim;
        }

    }
}
