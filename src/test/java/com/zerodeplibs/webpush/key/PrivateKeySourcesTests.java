package com.zerodeplibs.webpush.key;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

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

    private static final String PRIVATE_KEY_PKCS8_BYTES_BASE64 = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgry+xfRuVtpCPOmxc" +
        "aexkSXhua7zpaclMit3Un3ak+dqhRANCAARUzN0lXeB4j2/nljd8PovHUz/tC4X0" +
        "Sv4MSSN/2Tbx9ElHccgGa4uhw5ueoORaTRQ96SYmBFE4xJa3xBiMtfBR";

    @Test
    public void privateKeySourceCanExtractPrivateKeyFromPKCS8Bytes() {

        ECPrivateKey privateKey = PrivateKeySources.ofPKCS8Bytes(decodeBase64(PRIVATE_KEY_PKCS8_BYTES_BASE64))
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void privateKeySourceCanExtractPrivateKeyFromPEMString() {

        ECPrivateKey privateKey = PrivateKeySources.ofPEMString(PRIVATE_KEY_PEM_STRING)
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void privateKeySourceCanExtractPrivateKeyFromPKCS8Base64String() {

        ECPrivateKey privateKey = PrivateKeySources.ofPKCS8Baser64String(PRIVATE_KEY_PKCS8_BYTES_BASE64)
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void privateKeySourceCanExtractPrivateKeyFromPEMFile() throws URISyntaxException {

        ECPrivateKey privateKey = PrivateKeySources.ofPEMFile(Paths.get(this.getClass().getResource(PRIVATE_KEY_PEM_FILE_NAME).toURI()))
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    @Test
    public void privateKeySourceCanExtractPrivateKeyFromDERFile() throws URISyntaxException {

        ECPrivateKey privateKey = PrivateKeySources.ofDERFile(Paths.get(this.getClass().getResource(PRIVATE_KEY_DER_FILE_NAME).toURI()))
            .extract();

        assertThat(privateKey.getAlgorithm(), equalTo("EC"));
    }

    private byte[] decodeBase64(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }
}
