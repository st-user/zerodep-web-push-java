package com.zerodeplibs.webpush.jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;

public class DefaultVAPIDJWTGeneratorTests {


    @Test
    public void generate()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, JoseException,
        IOException {

        KeyPair keyPair = generateKeyPair();

        DefaultVAPIDJWTGenerator generator =
            new DefaultVAPIDJWTGenerator((ECPrivateKey) keyPair.getPrivate());

        Date dateClaim = new Date();
        Instant instantClaim = Instant.now();

        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfter(60, TimeUnit.SECONDS)
            .subject("mailto:test@example.com")
            .additionalClaim("adClaimString", "stringClaim")
            .additionalClaim("adClaimBoolean", true)
            .additionalClaim("adClaimInteger", 1)
            .additionalClaim("adClaimLong", 10L)
            .additionalClaim("adClaimDouble", 12345.12345d)
            .additionalClaim("adClaimDate", dateClaim)
            .additionalClaim("adClaimInstant", instantClaim)
            .build();

        String jwt = generator.generate(param);
        assertHeader(jwt);
        assertPayload(jwt, dateClaim, instantClaim);

        verifySign(jwt, keyPair.getPublic());
    }

    @Test
    public void shouldThrowExceptionWhenTypeOfAdditionalClaimIsNotAvaiable()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {


        String expectedMessage =
            "The value of an additional claim must be an instance of String, Boolean, Integer, Long, Double, Date or Instant.";

        DefaultVAPIDJWTGenerator generator =
            new DefaultVAPIDJWTGenerator((ECPrivateKey) generateKeyPair().getPrivate());


        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
            () -> generator.generate(createTestParamWith("a", new ArrayList<>())));

        assertThat(actualException.getMessage(), equalTo(expectedMessage));
    }

    private VAPIDJWTParam createTestParamWith(String name, Object claim) {
        return VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfter(60, TimeUnit.SECONDS)
            .subject("mailto:test@example.com")
            .additionalClaim(name, claim)
            .build();
    }

    private void assertHeader(String jwt) throws IOException {
        byte[] decoded = splitAndDecode(0, jwt);
        TestingJWTHeader actual =
            new ObjectMapper().readValue(decoded, TestingJWTHeader.class);

        assertThat(actual.getTyp(), equalTo("JWT"));
        assertThat(actual.getAlg(), equalTo("ES256"));
    }

    private void assertPayload(String jwt, Date origDateClaim, Instant origInstantClaim)
        throws IOException {
        byte[] decoded = splitAndDecode(1, jwt);
        TestingJWTPayload actual =
            new ObjectMapper().readValue(decoded, TestingJWTPayload.class);

        assertThat(actual.getAud(), equalTo("https://example.com"));
        assertThat(actual.getExp() > System.currentTimeMillis() / 1000, equalTo(true));
        assertThat(actual.getSub(), equalTo("mailto:test@example.com"));
        assertThat(actual.getAdClaimString(), equalTo("stringClaim"));
        assertThat(actual.getAdClaimBoolean(), equalTo(true));
        assertThat(actual.getAdClaimInteger(), equalTo(1));
        assertThat(actual.getAdClaimLong(), equalTo(10L));
        assertThat(actual.getAdClaimDouble(), equalTo(12345.12345d));
        assertThat(actual.getAdClaimDate(), equalTo((int) (origDateClaim.getTime() / 1000)));
        assertThat(actual.getAdClaimInstant(),
            equalTo((int) (origInstantClaim.toEpochMilli() / 1000)));
    }

    private void verifySign(String jwt, PublicKey publicKey) throws JoseException {

        JsonWebSignature verifier = new JsonWebSignature();
        verifier.setAlgorithmConstraints(new AlgorithmConstraints(
            AlgorithmConstraints.ConstraintType.WHITELIST,
            AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));

        verifier.setCompactSerialization(jwt);
        verifier.setKey(publicKey);

        assertThat(verifier.verifySignature(), equalTo(true));
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

        /* additional claims */
        private String adClaimString;
        private Boolean adClaimBoolean;
        private Integer adClaimInteger;
        private Long adClaimLong;
        private Double adClaimDouble;
        private Integer adClaimDate;
        private Integer adClaimInstant;

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

        public String getAdClaimString() {
            return adClaimString;
        }

        public void setAdClaimString(String adClaimString) {
            this.adClaimString = adClaimString;
        }

        public Boolean getAdClaimBoolean() {
            return adClaimBoolean;
        }

        public void setAdClaimBoolean(Boolean adClaimBoolean) {
            this.adClaimBoolean = adClaimBoolean;
        }

        public Integer getAdClaimInteger() {
            return adClaimInteger;
        }

        public void setAdClaimInteger(Integer adClaimInteger) {
            this.adClaimInteger = adClaimInteger;
        }

        public Long getAdClaimLong() {
            return adClaimLong;
        }

        public void setAdClaimLong(Long adClaimLong) {
            this.adClaimLong = adClaimLong;
        }

        public Double getAdClaimDouble() {
            return adClaimDouble;
        }

        public void setAdClaimDouble(Double adClaimDouble) {
            this.adClaimDouble = adClaimDouble;
        }

        public Integer getAdClaimDate() {
            return adClaimDate;
        }

        public void setAdClaimDate(Integer adClaimDate) {
            this.adClaimDate = adClaimDate;
        }

        public Integer getAdClaimInstant() {
            return adClaimInstant;
        }

        public void setAdClaimInstant(Integer adClaimInstant) {
            this.adClaimInstant = adClaimInstant;
        }
    }

}
