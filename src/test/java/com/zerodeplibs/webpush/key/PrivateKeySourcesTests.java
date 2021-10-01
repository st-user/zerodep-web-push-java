package com.zerodeplibs.webpush.key;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class PrivateKeySourcesTests {

    private static final String PRIVATE_KEY_PEM_FILE_NAME = "private_pkcs8.pem";
    private static final String PRIVATE_KEY_DER_FILE_NAME = "private_pkcs8.der";
    private static final String PRIVATE_KEY_PEM_STRING = "-----BEGIN PRIVATE KEY-----\r\n" +
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgry+xfRuVtpCPOmxc\r\n" +
        "aexkSXhua7zpaclMit3Un3ak+dqhRANCAARUzN0lXeB4j2/nljd8PovHUz/tC4X0\r\n" +
        "Sv4MSSN/2Tbx9ElHccgGa4uhw5ueoORaTRQ96SYmBFE4xJa3xBiMtfBR\r\n" +
        "-----END PRIVATE KEY-----";

    private static final String PRIVATE_KEY_PKCS8_BYTES_BASE64 =
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgry+xfRuVtpCPOmxc" +
            "aexkSXhua7zpaclMit3Un3ak+dqhRANCAARUzN0lXeB4j2/nljd8PovHUz/tC4X0" +
            "Sv4MSSN/2Tbx9ElHccgGa4uhw5ueoORaTRQ96SYmBFE4xJa3xBiMtfBR";

    @Test
    public void shouldExtractPrivateKeyFromPKCS8Bytes() {

        ECPrivateKey privateKey =
            PrivateKeySources.ofPKCS8Bytes(decodeBase64())
                .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPrivateKeyFromPEMText() {

        ECPrivateKey privateKey = PrivateKeySources.ofPEMText(PRIVATE_KEY_PEM_STRING)
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPrivateKeyFromPKCS8Base64Text() {

        ECPrivateKey privateKey =
            PrivateKeySources.ofPKCS8Base64Text(PRIVATE_KEY_PKCS8_BYTES_BASE64)
                .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPrivateKeyFromPEMFile()
        throws URISyntaxException, IOException {

        ECPrivateKey privateKey = PrivateKeySources.ofPEMFile(
                Paths.get(this.getClass().getResource(PRIVATE_KEY_PEM_FILE_NAME).toURI()))
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldExtractPrivateKeyFromDERFile()
        throws URISyntaxException, IOException {

        ECPrivateKey privateKey = PrivateKeySources.ofDERFile(
                Paths.get(this.getClass().getResource(PRIVATE_KEY_DER_FILE_NAME).toURI()))
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void shouldThrowExceptionWhenNullReferencesArePassed() {

        assertNullCheck(() -> PrivateKeySources.ofPKCS8Bytes(null),
            "pkcs8Bytes");

        assertNullCheck(() -> PrivateKeySources.ofPEMText(null),
            "pemText");

        assertNullCheck(() -> PrivateKeySources.ofPEMText(null, PEMParsers.ofStandard("Test")),
            "pemText");

        assertNullCheck(() -> PrivateKeySources.ofPEMText("ABC", null),
            "parser");

        assertNullCheck(() -> PrivateKeySources.ofPKCS8Base64Text(null),
            "pkcs8Base64Text");

        assertNullCheck(() -> PrivateKeySources.ofPEMFile(null),
            "path");

        assertNullCheck(() -> PrivateKeySources.ofPEMFile(null, PEMParsers.ofStandard("Test")),
            "path");

        assertNullCheck(() -> PrivateKeySources.ofPEMFile(new File(".").toPath(), null),
            "parser");

        assertNullCheck(() -> PrivateKeySources.ofDERFile(null),
            "path");

        assertNullCheck(() -> PrivateKeySources.ofECPrivateKey(null),
            "privateKey");

        assertNullCheck(() -> PrivateKeySources.getPEMFileSourceBuilder(null),
            "path");
    }

    @Test
    public void builderShouldThrowExceptionWhenNullReferencesArePassed() {

        assertNullCheck(
            () -> PrivateKeySources.getPEMFileSourceBuilder(new File(".").toPath()).charset(null),
            "charset");

        assertNullCheck(
            () -> PrivateKeySources.getPEMFileSourceBuilder(new File(".").toPath()).parser(null),
            "parser");

    }

    private byte[] decodeBase64() {
        return Base64.getDecoder().decode(PrivateKeySourcesTests.PRIVATE_KEY_PKCS8_BYTES_BASE64);
    }
}
