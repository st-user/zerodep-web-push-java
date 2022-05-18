package com.zerodeplibs.webpush;

import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateKeyPair;
import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.PrivateKeySource;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySource;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class VAPIDKeyPairTests {

    @BeforeAll
    public static void beforeAll() {
        JCAProviderInitializer.initialize();
    }

    @Test
    public void shouldExtractECPublicKeyInUncompressedForm()
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        VAPIDKeyPair vapidKeyPair = VAPIDKeyPairs.of(
            PrivateKeySources.ofECPrivateKey(privateKey),
            PublicKeySources.ofECPublicKey(publicKey),
            TestingJWTGeneraator::new
        );

        byte[] publicKeyEncoded = publicKey.getEncoded();

        byte[] expectedBytes = Arrays.copyOfRange(publicKeyEncoded, publicKeyEncoded.length - 65,
            publicKeyEncoded.length);
        assertThat(vapidKeyPair.extractPublicKeyInUncompressedForm(), equalTo(expectedBytes));

    }

    @Test
    public void shouldGenerateAuthorizationHeaderFieldWithTheGivenParameters()
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        VAPIDKeyPair vapidKeyPair = VAPIDKeyPairs.of(
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

    @Test
    public void shouldThrowExceptionWhenNullReferencesArePassed()
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        assertNullCheck(() -> VAPIDKeyPairs.of(
                null, PublicKeySources.ofECPublicKey(publicKey), TestingJWTGeneraator::new),
            "privateKeySource");

        assertNullCheck(() -> VAPIDKeyPairs.of(
                PrivateKeySources.ofECPrivateKey(privateKey), null, TestingJWTGeneraator::new),
            "publicKeySource");

        assertNullCheck(() -> VAPIDKeyPairs.of(
                PrivateKeySources.ofECPrivateKey(privateKey), PublicKeySources.ofECPublicKey(publicKey),
                null),
            "jwtGeneratorFactory");

        assertNullCheck(() -> VAPIDKeyPairs.of(
                new NullablePrivateKeySource(null), PublicKeySources.ofECPublicKey(publicKey),
                TestingJWTGeneraator::new),
            "The extracted private key");

        assertNullCheck(() -> VAPIDKeyPairs.of(
                PrivateKeySources.ofECPrivateKey(privateKey),
                new NullablePublicKeySource(null, new byte[] {0}),
                TestingJWTGeneraator::new),
            "The extracted public key");

        assertNullCheck(() -> VAPIDKeyPairs.of(
                PrivateKeySources.ofECPrivateKey(privateKey), PublicKeySources.ofECPublicKey(publicKey),
                (priv, pub) -> {
                    return null;
                }),
            "The VAPIDJWTGenerator created by the jwtGeneratorFactory");
    }

    @Test
    public void shouldUseDefaultImplementationWhenNoSubmoduleForVAPIDJWTGeneratorExists()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        KeyPair keyPair = generateKeyPair();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();

        VAPIDKeyPairs.of(PrivateKeySources.ofECPrivateKey(privateKey),
            PublicKeySources.ofECPublicKey(publicKey));
    }

    private static class TestingJWTGeneraator implements VAPIDJWTGenerator {

        private final ECPrivateKey privateKey;
        private final ECPublicKey publicKey;

        TestingJWTGeneraator(ECPrivateKey privateKey, ECPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        @Override
        public String generate(VAPIDJWTParam param) {
            return String.join(":",
                param.getOrigin(),
                param.getSubject().get(),
                param.getExpiresAt().toString(),
                privateKey.toString(),
                publicKey.toString()
            );
        }
    }

    private static class NullablePublicKeySource implements PublicKeySource {

        private final ECPublicKey publicKey;
        private final byte[] uncompressedBytes;

        NullablePublicKeySource(ECPublicKey publicKey, byte[] uncompressedBytes) {
            this.publicKey = publicKey;
            this.uncompressedBytes = uncompressedBytes;
        }

        @Override
        public ECPublicKey extract() throws InvalidECPublicKeyException {
            return this.publicKey;
        }

        @Override
        public byte[] extractBytesInUncompressedForm() throws InvalidECPublicKeyException {
            return this.uncompressedBytes;
        }
    }

    private static class NullablePrivateKeySource implements PrivateKeySource {

        private final ECPrivateKey privateKey;

        NullablePrivateKeySource(ECPrivateKey privateKey) {
            this.privateKey = privateKey;
        }

        @Override
        public ECPrivateKey extract() {
            return this.privateKey;
        }
    }

}
