package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;

/**
 * Static factory methods for instantiating an implementation class of {@link PrivateKeySource}.
 *
 * <p>
 * <b>Examples:</b><br>
 * The following is an example of commands that uses OpenSSL
 * to generate a file that can be handled with a PrivateKeySource.
 *
 * <pre class="code">
 * openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
 * openssl pkcs8 -in private.pem -topk8 -nocrypt -out my-private_pkcs8.pem
 * </pre>
 * If you want to generate in DER format, you can also do as follows.
 * <pre class="code">
 * openssl pkcs8 -in private.pem -topk8 -nocrypt -outform der -out my-private_pkcs8.der
 * </pre>
 * The examples of using these files to create PrivateKeySource is as follows.
 * <pre class="code">
 * Path pemPath = new File("my-private_pkcs8.pem").toPath();
 * Path derPath = new FIle("my-private_pkcs8.der").toPath();
 *
 * PrivateKeySource pemSource = PrivateKeySources.ofPEMFile(pemPath);
 * PrivateKeySource derSource = PrivateKeySources.ofDerFile(derPath);
 *
 * byte[] pemBytes = Files.readAllBytes(pemPath);
 * String pemString = new String(pemBytes, StandardCharsets.UTF_8);
 * PrivateKeySource pemSource2 = PrivateKeySources.ofPEMString(pemString);
 *
 * byte[] derBytes = Files.readAllBytes(derPath);
 * PrivateKeySource derSource2 = PrivateKeySources.ofPKCS8Bytes(derBytes);
 *
 * </pre>
 * </p>
 *
 * @author Tomoki Sato
 * @see PrivateKeySource
 * @see PublicKeySource
 * @see PublicKeySources
 */
public class PrivateKeySources {

    private PrivateKeySources() {
    }

    /**
     * Creates a PrivateKeySource with the byte array
     * that is assumed to be encoded according to the PKCS#8 standard.
     *
     * @param pkcs8Bytes the byte array representing a private key.
     * @return a new PrivateKeySource.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    // BEGIN CHECK STYLE OFF
    public static PrivateKeySource ofPKCS8Bytes(byte[] pkcs8Bytes) { // END CHECK STYLE OFF
        return new BytesPrivateKeySource(pkcs8Bytes);
    }

    /**
     * Creates a PrivateKeySource with the PEM string.
     * The underlying binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The PEM string is assumed to start with '-----BEGIN PRIVATE KEY-----'
     * and end with '-----END PRIVATE KEY-----'.
     * </p>
     *
     * @param pemString the PEM string representing a private key.
     * @return a new PrivateKeySource.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    public static PrivateKeySource ofPEMString(String pemString) {
        return ofPEMString(pemString, PEMParsers.ofStandard(PEMParser.PKCS8_PRIVATE_KEY_LABEL));
    }

    /**
     * Creates a PrivateKeySource with the PEM string and the {@link PEMParser}.
     * The underlying binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The PEM string are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param pemString the PEM string representing a private key.
     * @param parser    the parser for parsing the PEM string.
     * @return a new PrivateKeySource.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    public static PrivateKeySource ofPEMString(String pemString, PEMParser parser) {
        return ofPKCS8Bytes(parser.parse(pemString));
    }

    /**
     * Creates a PrivateKeySource with the base64-encoded private key
     * (<b>NOT</b> base64<b>url</b>-encoded).
     * The binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * @param pkcs8Base64String the base64-encoded private key.
     * @return a new PrivateKeySource.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    // BEGIN CHECK STYLE OFF
    public static PrivateKeySource ofPKCS8Base64String(
        String pkcs8Base64String) { // END CHECK STYLE OFF
        return ofPKCS8Bytes(Base64.getDecoder().decode(pkcs8Base64String));
    }

    /**
     * Creates a PrivateKeySource with the PEM formatted file specified by the path.
     * The underlying binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The first line of the file is assumed to be '-----BEGIN PRIVATE KEY-----'
     * and the end line of the file is assumed to be '-----END PRIVATE KEY-----'.
     * </p>
     *
     * @param path the path to the PEM formatted file.
     * @return a new PrivateKeySource.
     */
    public static PrivateKeySource ofPEMFile(Path path) {
        return getPEMFileSourceBuilder(path).build();
    }

    /**
     * Creates a PrivateKeySource with the PEM formatted file specified by the path.
     * The underlying binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The contents of the PEM file are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param path   the path to the PEM formatted file.
     * @param parser the parser for parsing the contents of the PEM file.
     * @return a new PrivateKeySource.
     */
    public static PrivateKeySource ofPEMFile(Path path, PEMParser parser) {
        return getPEMFileSourceBuilder(path).parser(parser).build();
    }

    /**
     * Creates a PrivateKeySource with the DER file specified by the path.
     * Its binary data is assumed to be encoded according to the PKCS#8 standard.
     *
     * @param path the path to the DER file.
     * @return a new PrivateKeySource.
     */
    public static PrivateKeySource ofDERFile(Path path) {
        return ofPKCS8Bytes(FileUtil.readAllBytes(path));
    }

    /**
     * Creates a PrivateKeySource that wraps the given ECPrivateKey object.
     *
     * @param privateKey the ECPrivateKey.
     * @return a new PrivateKeySource.
     */
    public static PrivateKeySource ofECPrivateKey(ECPrivateKey privateKey) {
        return new KeyObjectPrivateKeySource(privateKey);
    }

    /**
     * Gets a new PEMFileSourceBuilder.
     *
     * @param pemFilePath the path to the PEM formatted file.
     * @return a new PEMFileSourceBuilder.
     */
    public static PEMFileSourceBuilder getPEMFileSourceBuilder(Path pemFilePath) {
        return new PEMFileSourceBuilder(pemFilePath);
    }

    /**
     * The builder class for creating instances of the PrivateKeySource from PEM formatted files.
     *
     * @author Tomoki Sato
     */
    public static class PEMFileSourceBuilder {
        private final Path pemFilePath;
        private Charset charset = StandardCharsets.UTF_8;
        private PEMParser parser = PEMParsers.ofStandard(PEMParser.PKCS8_PRIVATE_KEY_LABEL);

        PEMFileSourceBuilder(Path pemFilePath) {
            this.pemFilePath = pemFilePath;
        }

        /**
         * Specifies the encoding of the PEM file.
         *
         * @param charset the encoding.
         * @return this object.
         */
        public PEMFileSourceBuilder charset(Charset charset) {
            WebPushPreConditions.checkNotNull(charset, "charset");
            this.charset = charset;
            return this;
        }

        /**
         * Specifies the parser for parsing the contents of the PEM file.
         *
         * @param parser the parser for parsing the contents of the PEM file.
         * @return this object.
         */
        public PEMFileSourceBuilder parser(PEMParser parser) {
            WebPushPreConditions.checkNotNull(parser, "parser");
            this.parser = parser;
            return this;
        }

        /**
         * Creates a new PrivateKeySource.
         *
         * @return a new PrivateKeySource.
         */
        public PrivateKeySource build() {
            return PrivateKeySources.ofPEMString(
                FileUtil.readAsString(pemFilePath, charset),
                parser
            );
        }
    }


}
