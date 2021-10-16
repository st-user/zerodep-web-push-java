package com.zerodeplibs.webpush.ext.jwt.nimbusjose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
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

public class NimbusJoseVAPIDJWTGeneratorTests {

    @Test
    public void generate()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException,
        IOException {

        KeyPair keyPair = generateKeyPair();

        NimbusJoseVAPIDJWTGenerator generator =
            new NimbusJoseVAPIDJWTGenerator((ECPrivateKey) keyPair.getPrivate());

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(60)
            .subject("mailto:test@example.com")
            .additionalClaim("adClaim", new MyAdditionalClaim("hello"))
            .build();

        String jwt = generator.generate(param);
        verifySign(jwt, keyPair.getPublic());
        assertHeader(jwt);
        assertPayload(jwt);
    }

    private void assertHeader(String jwt) throws IOException {
        byte[] decoded = splitAndDecode(0, jwt);
        TestingJWTHeader actual =
            new ObjectMapper().readValue(decoded, TestingJWTHeader.class);

        assertEquals("JWT", actual.getTyp());
        assertEquals("ES256", actual.getAlg());
    }

    private void assertPayload(String jwt) throws IOException {
        byte[] decoded = splitAndDecode(1, jwt);
        TestingJWTPayload actual =
            new ObjectMapper().readValue(decoded, TestingJWTPayload.class);

        assertEquals("https://example.com", actual.getAud());
        assertTrue(actual.getExp() > System.currentTimeMillis() / 1000);
        assertEquals("mailto:test@example.com", actual.getSub());
        assertEquals("hello", actual.getAdClaim().getMyClaim());
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

    public static class TestingJWTHeader {

        private String typ;
        private String alg;

        public String getTyp() {
            return typ;
        }

        public void setTyp(String typ) {
            this.typ = typ;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }
    }

    public static class TestingJWTPayload {

        private String aud;
        private long exp;
        private String sub;
        private MyAdditionalClaim adClaim;

        public String getAud() {
            return aud;
        }

        public void setAud(String aud) {
            this.aud = aud;
        }

        public long getExp() {
            return exp;
        }

        public void setExp(long exp) {
            this.exp = exp;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }

        public MyAdditionalClaim getAdClaim() {
            return adClaim;
        }

        public void setAdClaim(MyAdditionalClaim adClaim) {
            this.adClaim = adClaim;
        }
    }

    public static class MyAdditionalClaim {

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
