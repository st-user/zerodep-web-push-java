package com.zerodeplibs.webpush.key;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
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

    @Test
    public void publicKeySourceCanExtractPublicKeyFromUncompressedBytes() {

        ECPublicKey publicKey =
            PublicKeySources.ofUncompressedBytes(decodeBase64(PUBLIC_KEY_UNCOMPRESSED_BYTES_BASE64))
                .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromX509Bytes() {

        ECPublicKey publicKey =
            PublicKeySources.ofX509Bytes(decodeBase64(PUBLIC_KEY_X509_BYTES_BASE64))
                .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromPEMString() {

        ECPublicKey publicKey = PublicKeySources.ofPEMText(PUBLIC_KEY_PEM_STRING)
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromUncompressedBase64String() {

        ECPublicKey publicKey =
            PublicKeySources.ofUncompressedBase64Text(PUBLIC_KEY_UNCOMPRESSED_BYTES_BASE64)
                .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromX509Base64String() {

        ECPublicKey publicKey = PublicKeySources.ofX509Base64Text(PUBLIC_KEY_X509_BYTES_BASE64)
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromPEMFile()
        throws URISyntaxException, IOException {
        ECPublicKey publicKey = PublicKeySources.ofPEMFile(
                Paths.get(this.getClass().getResource(PUBLIC_KEY_PEM_FILE_NAME).toURI()))
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void publicKeySourceCanExtractPublicKeyFromDERFile()
        throws URISyntaxException, IOException {
        ECPublicKey publicKey = PublicKeySources.ofDERFile(
                Paths.get(this.getClass().getResource(PUBLIC_KEY_DER_FILE_NAME).toURI()))
            .extract();

        assertThat(publicKey.getAlgorithm(), equalTo("EC"));
    }


    private byte[] decodeBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
