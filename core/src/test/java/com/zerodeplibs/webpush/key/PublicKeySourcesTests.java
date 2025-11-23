package com.zerodeplibs.webpush.key;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.JCAProviderInitializer;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class PublicKeySourcesTests {

    private static final String PUBLIC_KEY_PEM_FILE_NAME = "pub_uncompressed.pem";
    private static final String PUBLIC_KEY_DER_FILE_NAME = "pub_uncompressed.der";

    private static final String PUBLIC_KEY_PEM_STRING = "-----BEGIN PUBLIC KEY-----\r\n" +
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEVMzdJV3geI9v55Y3fD6Lx1M/7QuF\r\n" +
        "9Er+DEkjf9k28fRJR3HIBmuLocObnqDkWk0UPekmJgRROMSWt8QYjLXwUQ==\r\n" +
        "-----END PUBLIC KEY-----\r\n";

    private static final String PUBLIC_KEY_X509_BYTES_BASE64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEVMzdJV3geI9v55Y3fD6Lx1M/7QuF9Er+DEkjf9k28fRJR3HIBmuLocObnqDkWk0UPekmJgRROMSWt8QYjLXwUQ==";
    private static final String PUBLIC_KEY_UNCOMPRESSED_BYTES_BASE64 =
        "BFTM3SVd4HiPb+eWN3w+i8dTP+0LhfRK/gxJI3/ZNvH0SUdxyAZri6HDm56g5FpNFD3pJiYEUTjElrfEGIy18FE=";

    @BeforeAll
    public static void beforeAll() {
        JCAProviderInitializer.initialize();
    }

    @Test
    public void shouldExtractPublicKeyFromUncompressedBytes() {

        ECPublicKey publicKey =
            PublicKeySources.ofUncompressedBytes(decodeBase64(PUBLIC_KEY_UNCOMPRESSED_BYTES_BASE64))
                .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPublicKeyFromX509Bytes() {

        ECPublicKey publicKey =
            PublicKeySources.ofX509Bytes(decodeBase64(PUBLIC_KEY_X509_BYTES_BASE64))
                .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPublicKeyFromPEMText() {

        ECPublicKey publicKey = PublicKeySources.ofPEMText(PUBLIC_KEY_PEM_STRING)
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPublicKeyFromPEMFile()
        throws URISyntaxException, IOException {
        ECPublicKey publicKey = PublicKeySources.ofPEMFile(
                Paths.get(this.getClass().getResource(PUBLIC_KEY_PEM_FILE_NAME).toURI()))
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPublicKeyFromDERFile()
        throws URISyntaxException, IOException {
        ECPublicKey publicKey = PublicKeySources.ofDERFile(
                Paths.get(this.getClass().getResource(PUBLIC_KEY_DER_FILE_NAME).toURI()))
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }


    @Test
    public void shouldThrowExceptionWhenNullReferencesArePassed() {

        assertNullCheck(() -> PublicKeySources.ofUncompressedBytes(null),
            "uncompressedBytes");

        assertNullCheck(() -> PublicKeySources.ofX509Bytes(null),
            "x509Bytes");

        assertNullCheck(() -> PublicKeySources.ofPEMText(null),
            "pemText");

        assertNullCheck(() -> PublicKeySources.ofPEMText(null, PEMParsers.ofStandard("Test")),
            "pemText");

        assertNullCheck(() -> PublicKeySources.ofPEMText("ABC", null),
            "parser");

        assertNullCheck(() -> PublicKeySources.ofPEMFile(null),
            "path");

        assertNullCheck(() -> PublicKeySources.ofPEMFile(null, PEMParsers.ofStandard("Test")),
            "path");

        assertNullCheck(
            () -> PublicKeySources.ofPEMFile(new File(".").toPath(), null),
            "parser");

        assertNullCheck(
            () -> PublicKeySources.ofDERFile(null),
            "path");

        assertNullCheck(
            () -> PublicKeySources.ofECPublicKey(null),
            "publicKey");

        assertNullCheck(
            () -> PublicKeySources.getPEMFileSourceBuilder(null),
            "path");
    }

    @Test
    public void builderShouldThrowExceptionWhenNullReferencesArePassed() {

        assertNullCheck(
            () -> PublicKeySources.getPEMFileSourceBuilder(new File(".").toPath()).charset(null),
            "charset");

        assertNullCheck(
            () -> PublicKeySources.getPEMFileSourceBuilder(new File(".").toPath()).parser(null),
            "parser");

    }

    private byte[] decodeBase64(String Base64Text) {
        return Base64.getDecoder().decode(Base64Text);
    }
}
