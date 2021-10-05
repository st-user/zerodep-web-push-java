package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.interfaces.ECPrivateKey;

/**
 * Static factory methods for {@link PrivateKeySource}.
 *
 * <h3>Examples:</h3>
 * <p>
 * The following is an example of commands that uses OpenSSL
 * to generate a file that can be handled with {@link PrivateKeySource}s.
 * </p>
 *
 * <pre class="code">
 * openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
 * openssl pkcs8 -in my-private.pem -topk8 -nocrypt -out my-private_pkcs8.pem
 * </pre>
 *
 * <p>
 * If you want to generate in DER format, you can also do as follows.
 * </p>
 * <pre class="code">
 * openssl pkcs8 -in my-private.pem -topk8 -nocrypt -outform der -out my-private_pkcs8.der
 * </pre>
 *
 * <p>
 * The examples of using these files to create {@link PrivateKeySource}s are as follows.
 * </p>
 * <pre class="code">
 * Path pemPath = new File("my-private_pkcs8.pem").toPath();
 * Path derPath = new File("my-private_pkcs8.der").toPath();
 *
 * PrivateKeySource pemSource = PrivateKeySources.ofPEMFile(pemPath);
 * PrivateKeySource derSource = PrivateKeySources.ofDERFile(derPath);
 *
 * byte[] pemBytes = Files.readAllBytes(pemPath);
 * String pemText = new String(pemBytes, StandardCharsets.UTF_8);
 * PrivateKeySource pemSource2 = PrivateKeySources.ofPEMText(pemText);
 *
 * byte[] derBytes = Files.readAllBytes(derPath);
 * PrivateKeySource derSource2 = PrivateKeySources.ofPKCS8Bytes(derBytes);
 *
 * </pre>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * The factory methods themselves are thread-safe,
 * but the returned objects are <b>NOT</b> thread-safe.
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
     * Creates a new {@link PrivateKeySource} with the given octet sequence
     * that is assumed to be encoded according to the PKCS#8 standard.
     *
     * @param pkcs8Bytes the octet sequence representing a private key.
     * @return a new {@link PrivateKeySource}.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    // BEGIN CHECK STYLE OFF
    public static PrivateKeySource ofPKCS8Bytes(byte[] pkcs8Bytes) { // END CHECK STYLE OFF
        return new BytesPrivateKeySource(pkcs8Bytes);
    }

    /**
     * Creates a new {@link PrivateKeySource} with the given PEM-encoded text.
     * The underlying octet sequence is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * the PEM-encoded text is assumed to contain a private key data
     * that starts with '-----BEGIN PRIVATE KEY-----'
     * and ends with '-----END PRIVATE KEY-----'.
     * </p>
     *
     * @param pemText the PEM-encoded text containing a private key data.
     * @return a new {@link PrivateKeySource}.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    public static PrivateKeySource ofPEMText(String pemText) {
        return ofPEMText(pemText, PEMParsers.ofStandard(PEMParser.PKCS8_PRIVATE_KEY_LABEL));
    }

    /**
     * Creates a new {@link PrivateKeySource} with the given PEM-encoded text
     * and the given {@link PEMParser}.
     * The underlying octet sequence is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * the PEM-encoded text are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param pemText the PEM-encoded text representing a private key.
     * @param parser  the parser for parsing the PEM-encoded text.
     * @return a new {@link PrivateKeySource}.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.PKCS8EncodedKeySpec
     */
    public static PrivateKeySource ofPEMText(String pemText, PEMParser parser) {
        WebPushPreConditions.checkNotNull(parser, "parser");
        return ofPKCS8Bytes(parser.parse(pemText));
    }

    /**
     * Creates a new {@link PrivateKeySource} with the PEM formatted file specified
     * by the given path.
     * The underlying octet sequence is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The PEM formatted file is assumed to contain a private key data
     * that starts with '-----BEGIN PRIVATE KEY-----'
     * and ends with '-----END PRIVATE KEY-----'.
     * </p>
     *
     * @param path the path to a PEM formatted file.
     * @return a new {@link PrivateKeySource}.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the contents of the given file
     *                               cannot be parsed as a valid PEM format.
     */
    public static PrivateKeySource ofPEMFile(Path path) throws IOException {
        return getPEMFileSourceBuilder(path).build();
    }

    /**
     * Creates a new {@link PrivateKeySource} with the PEM formatted file specified
     * by the given path.
     * The underlying octet sequence is assumed to be encoded according to the PKCS#8 standard.
     *
     * <p>
     * The contents of the PEM file are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param path   the path to a PEM formatted file.
     * @param parser a parser for parsing the contents of the PEM file.
     * @return a new {@link PrivateKeySource}.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the contents of the given file
     *                               cannot be parsed as a valid PEM format.
     */
    public static PrivateKeySource ofPEMFile(Path path, PEMParser parser) throws IOException {
        return getPEMFileSourceBuilder(path).parser(parser).build();
    }

    /**
     * Creates a new {@link PrivateKeySource} with the DER file specified by the given path.
     * Its octet sequence is assumed to be encoded according to the PKCS#8 standard.
     *
     * @param path the path to a DER file.
     * @return a new {@link PrivateKeySource}.
     * @throws IOException if an I/O error occurs.
     */
    public static PrivateKeySource ofDERFile(Path path) throws IOException {
        return ofPKCS8Bytes(FileUtil.readAllBytes(path));
    }

    /**
     * Creates a new {@link PrivateKeySource} that wraps the given {@link ECPrivateKey} object.
     *
     * @param privateKey an {@link ECPrivateKey} object.
     * @return a new {@link PrivateKeySource}.
     */
    public static PrivateKeySource ofECPrivateKey(ECPrivateKey privateKey) {
        return new KeyObjectPrivateKeySource(privateKey);
    }

    /**
     * Gets a new {@link PEMFileSourceBuilder}.
     *
     * @param path the path to a PEM formatted file.
     * @return a new {@link PEMFileSourceBuilder}.
     */
    public static PEMFileSourceBuilder getPEMFileSourceBuilder(Path path) {
        return new PEMFileSourceBuilder(path);
    }

    /**
     * The builder class for creating {@link PrivateKeySource} from PEM formatted files.
     *
     * @author Tomoki Sato
     */
    public static class PEMFileSourceBuilder {
        private final Path path;
        private Charset charset = StandardCharsets.UTF_8;
        private PEMParser parser = PEMParsers.ofStandard(PEMParser.PKCS8_PRIVATE_KEY_LABEL);

        PEMFileSourceBuilder(Path path) {
            WebPushPreConditions.checkNotNull(path, "path");
            this.path = path;
        }

        /**
         * Specifies the encoding of the PEM file.
         *
         * @param charset the encoding of the PEM file.
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
         * Creates a new {@link PrivateKeySource}.
         *
         * @return a new {@link PrivateKeySource}.
         * @throws IOException           if an I/O error occurs.
         * @throws MalformedPEMException if the contents of the file
         *                               cannot be parsed as a valid PEM format.
         */
        public PrivateKeySource build() throws IOException {
            return PrivateKeySources.ofPEMText(
                FileUtil.readAsString(path, charset),
                parser
            );
        }
    }


}
