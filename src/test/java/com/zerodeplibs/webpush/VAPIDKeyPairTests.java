package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class VAPIDKeyPairTests {


    private static class TestingJWTGeneraator implements VAPIDJWTGenerator {

        private ECPrivateKey privateKey;
        private ECPublicKey publicKey;

        TestingJWTGeneraator(ECPrivateKey privateKey, ECPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        @Override
        public String generate(VAPIDJWTParam param) {
            String ret = Stream.of(
                param.getOrigin(),
                param.getSubject().get(),
                param.getExpiresAt().toString(),
                privateKey.toString(),
                publicKey.toString()
            ).collect(Collectors.joining(":"));
            return ret;
        }
    }

    @Test
    public void uncompressedPublicKeyBytesShouldReturnBytesInUncompressedForm()
        throws NoSuchAlgorithmException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        VAPIDKeyPair vapidKeyPair = new VAPIDKeyPair(
            PrivateKeySources.ofECPrivateKey(privateKey),
            PublicKeySources.ofECPublicKey(publicKey),
            TestingJWTGeneraator::new
        );

        byte[] publicKeyEncoded = publicKey.getEncoded();


        byte[] expectedBytes = Arrays.copyOfRange(publicKeyEncoded, publicKeyEncoded.length - 65,
            publicKeyEncoded.length);
        assertThat(vapidKeyPair.extractUncompressedPublicKeyBytes(), equalTo(expectedBytes));

    }

    @Test
    public void createAuthorizationHeaderValueShouldCreateValueFromKeyPairAndJWTParam()
        throws NoSuchAlgorithmException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        VAPIDKeyPair vapidKeyPair = new VAPIDKeyPair(
            PrivateKeySources.ofECPrivateKey(privateKey),
            PublicKeySources.ofECPublicKey(publicKey),
            TestingJWTGeneraator::new
        );

        String origin = "https://example.com/origin";
        Date expiresAt = new Date();
        String subject = "subjectX";
        VAPIDJWTParam jwtParam = VAPIDJWTParam.getBuilder()
            .resourceURLString(origin)
            .expiresAt(expiresAt)
            .subject(subject)
            .build();

        byte[] publicKeyEncoded = publicKey.getEncoded();
        byte[] uncompressedBytes =
            Arrays.copyOfRange(publicKeyEncoded, publicKeyEncoded.length - 65,
                publicKeyEncoded.length);

        String expectedTokenString = Stream.of(
            "https://example.com",
            subject,
            expiresAt.toString(),
            privateKey.toString(),
            publicKey.toString()
        ).collect(Collectors.joining(":"));
        String expectedPublicKeyString =
            Base64.getUrlEncoder().withoutPadding().encodeToString(uncompressedBytes);
        String expectedString =
            String.format("vapid t=%s, k=%s", expectedTokenString, expectedPublicKeyString);
        assertThat(vapidKeyPair.generateAuthorizationHeaderValue(jwtParam),
            equalTo(expectedString));
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        return keyPairGenerator.generateKeyPair();
    }
}
