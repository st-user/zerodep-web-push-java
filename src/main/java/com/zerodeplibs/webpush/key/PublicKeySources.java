package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Static factory methods for instantiating an implementation class of {@link PublicKeySource}.
 *
 * <p>
 * PublicKeySource created by the static factory methods
 * of this class performs public key validation during extraction.
 * It uses the validation method described in Section 5.6.2.3 of <a href="https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-56Ar2.pdf">Recommendation for Pair-Wise Key Establishment Schemes Using Discrete Logarithm Cryptography - NIST Special Publication 800-56A Revision 2</a>.
 * </p>
 *
 * <p>
 * <b>Examples:</b><br>
 * The following is an example of commands that uses OpenSSL
 * to generate a file that can be handled with a PublicKeySource.
 *
 * <pre class="code">
 * openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
 * openssl ec -in my-private.pem -pubout -conv_form uncompressed -out my-pub.pem
 * </pre>
 * If you want to generate in DER format, you can also do as follows.
 * <pre class="code">
 * openssl ec -in my-private.pem -pubout -conv_form uncompressed -outform der -out my-pub.der
 * </pre>
 * The examples of using these files to create PublicKeySource is as follows.
 * <pre class="code">
 * Path pemPath = new File("my-pub.pem").toPath();
 * Path derPath = new FIle("my-pub.der").toPath();
 *
 * PublicKeySource pemSource = PublicKeySources.ofPEMFile(pemPath);
 * PublicKeySource derSource = PublicKeySources.ofDerFile(derPath);
 *
 * byte[] pemBytes = Files.readAllBytes(pemPath);
 * String pemString = new String(pemBytes, StandardCharsets.UTF_8);
 * PublicKeySource pemSource2 = PublicKeySources.ofPEMString(pemString);
 *
 * byte[] derBytes = Files.readAllBytes(derPath);
 * PublicKeySource derSource2 = PublicKeySources.ofX509Bytes(derBytes);
 *
 * </pre>
 * </p>
 *
 * @author Tomoki Sato
 * @see PublicKeySource
 * @see PublicKeySourceFactory
 * @see InvalidECPublicKeyException
 * @see PrivateKeySource
 * @see PrivateKeySources
 */
public class PublicKeySources {

    private PublicKeySources() {
    }

    /**
     * Create a PublicKeySource with the byte array representing
     * a public key on the P-256 curve encoded in the uncompressed form[X9.62].
     *
     * @param uncompressedBytes the byte array representing a public key.
     * @return a new PublicKeySource.
     * @throws MalformedUncompressedBytesException if the given array doesn't start with 0x4
     *                                             or the length isn't 65 bytes.
     */
    public static PublicKeySource ofUncompressedBytes(byte[] uncompressedBytes) {
        return withValidation().fromUncompressedBytes(uncompressedBytes);
    }

    /**
     * Creates a PublicKeySource with the byte array
     * that is assumed to be encoded according to the X.509 standard.
     *
     * @param x509Bytes the byte array representing a public key.
     * @return a new PublicKeySource.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofX509Bytes(byte[] x509Bytes) {
        return withValidation().fromX509Bytes(x509Bytes);
    }

    /**
     * Creates a PublicKeySource with the PEM-encoded text.
     * The underlying binary data is assumed to be encoded according to the X.509 standard.
     *
     * <p>
     * the PEM-encoded text is assumed to start with '-----BEGIN PUBLIC KEY-----'
     * and end with '-----END PUBLIC KEY-----'.
     * </p>
     *
     * @param pemText the PEM-encoded text representing a public key.
     * @return a new PublicKeySource.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofPEMText(String pemText) {
        return ofPEMText(pemText,
            PEMParsers.ofStandard(PEMParser.SUBJECT_PUBLIC_KEY_INFO_LABEL));
    }

    /**
     * Creates a PublicKeySource with the PEM-encoded text and the {@link PEMParser}.
     * The underlying binary data is assumed to be encoded according to the X.509 standard.
     *
     * <p>
     * the PEM-encoded text are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param pemText the PEM-encoded text representing a public key.
     * @param parser  the parser for parsing the PEM-encoded text.
     * @return a new PublicKeySource.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofPEMText(String pemText, PEMParser parser) {
        return ofX509Bytes(parser.parse(pemText));
    }

    /**
     * Creates a PublicKeySource with the base64-encoded public key
     * (<b>NOT</b> base64<b>url</b>-encoded).
     * The binary data is assumed to represent a public key
     * on the P-256 curve that encoded in the uncompressed form[X9.62].
     *
     * @param uncompressedBytesBase64 the base64-encoded public key.
     * @return a new PublicKeySource.
     * @throws MalformedUncompressedBytesException if the given array doesn't start with 0x4
     *                                             or the length isn't 65 bytes.
     * @throws IllegalArgumentException            if the given text is not in valid Base64 scheme.
     */
    public static PublicKeySource ofUncompressedBase64Text(String uncompressedBytesBase64) {
        return ofUncompressedBytes(Base64.getDecoder().decode(uncompressedBytesBase64));
    }

    /**
     * Creates a PublicKeySource with the base64-encoded public key
     * (<b>NOT</b> base64<b>url</b>-encoded).
     * The binary data is assumed to be encoded according to the X.509 standard.
     *
     * @param x509Base64Text the base64-encoded public key.
     * @return a new PublicKeySource.
     * @throws IllegalArgumentException if the given text is not in valid Base64 scheme.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofX509Base64Text(String x509Base64Text) {
        return ofX509Bytes(Base64.getDecoder().decode(x509Base64Text));
    }

    /**
     * Creates a PublicKeySource with the PEM formatted file specified by the path.
     * The underlying binary data is assumed to be encoded according to the X.509 standard.
     *
     * <p>
     * The first line of the file is assumed to be '-----BEGIN PUBLIC KEY-----'
     * and the end line of the file is assumed to be '-----END PUBLIC KEY-----'.
     * </p>
     *
     * @param path the path to the PEM formatted file.
     * @return a new PublicKeySource.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     */
    public static PublicKeySource ofPEMFile(Path path) throws IOException {
        return getPEMFileSourceBuilder(path).build();
    }

    /**
     * Creates a PublicKeySource with the PEM formatted file specified by the path.
     * The underlying binary data is assumed to be encoded according to the X.509 standard.
     *
     * <p>
     * The contents of the PEM file are parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param path   the path to the PEM formatted file.
     * @param parser the parser for parsing the contents of the PEM file.
     * @return a new PublicKeySource.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     */
    public static PublicKeySource ofPEMFile(Path path, PEMParser parser) throws IOException {
        return getPEMFileSourceBuilder(path).parser(parser).build();
    }

    /**
     * Creates a PublicKeySource with the DER file specified by the path.
     * Its binary data is assumed to be encoded according to the X.509 standard.
     *
     * @param path the path to the DER file.
     * @return a new PublicKeySource.
     * @throws IOException if an I/O error occurs.
     */
    public static PublicKeySource ofDERFile(Path path) throws IOException {
        return ofX509Bytes(FileUtil.readAllBytes(path));
    }

    /**
     * Creates a PublicKeySource that wraps the given ECPublicKey object.
     *
     * @param publicKey the ECPublicKey.
     * @return a new PublicKeySource.
     */
    public static PublicKeySource ofECPublicKey(ECPublicKey publicKey) {
        return withValidation().fromECPublicKey(publicKey);
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
     * The builder class for creating instances of the PublicKeySource from PEM formatted files.
     *
     * @author Tomoki Sato
     */
    public static class PEMFileSourceBuilder {
        private final Path pemFilePath;
        private Charset charset = StandardCharsets.UTF_8;
        private PEMParser parser = PEMParsers.ofStandard(PEMParser.SUBJECT_PUBLIC_KEY_INFO_LABEL);
        private PublicKeySourceFactory factory = withValidation();

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
         * Specifies the factory to use when creating the PublicKeySource.
         *
         * @param factory the factory to use.
         * @return this object.
         */
        public PEMFileSourceBuilder factory(PublicKeySourceFactory factory) {
            WebPushPreConditions.checkNotNull(factory, "factory");
            this.factory = factory;
            return this;
        }

        /**
         * Creates a new PublicKeySource.
         *
         * @return a new PublicKeySource.
         * @throws IOException           if an I/O error occurs.
         * @throws MalformedPEMException if the given text
         *                               cannot be parsed as a valid PEM format.
         */
        public PublicKeySource build() throws IOException {
            byte[] parsed = this.parser.parse(FileUtil.readAsString(pemFilePath, charset));
            return this.factory.fromX509Bytes(parsed);
        }

    }

    /**
     * Returns an instance of the factory that performs public key validation.
     *
     * @return the factory that performs public key validation.
     * @see PublicKeySources
     */
    public static PublicKeySourceFactory withValidation() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Returns an instance of the factory that does not perform public key validation.
     *
     * <br>
     * <p>
     * <b>WARNING</b>:
     * It is not recommended that you use this factory
     * unless you have a clear reason for not requiring public key validation.
     * </P>
     *
     * @return the factory that does not perform public key validation.
     */
    public static PublicKeySourceFactory noValidation() {
        return NO_VALIDATION_INSTANCE;
    }

    private static final PublicKeySourceFactory DEFAULT_INSTANCE =
        new FactoryImpl(ECPublicKeyUtil::validateECPublicKey);
    private static final PublicKeySourceFactory NO_VALIDATION_INSTANCE = new FactoryImpl(pub -> {
    });

    private static class FactoryImpl implements PublicKeySourceFactory {

        private final Consumer<ECPublicKey> validator;

        FactoryImpl(Consumer<ECPublicKey> validator) {
            this.validator = validator;
        }

        @Override
        public PublicKeySource fromUncompressedBytes(byte[] uncompressedBytes) {
            return BytesPublicKeySource.ofUncompressed(uncompressedBytes, this.validator);
        }

        @Override
        public PublicKeySource fromX509Bytes(byte[] x509Bytes) {
            return BytesPublicKeySource.ofX509(x509Bytes, this.validator);
        }

        @Override
        public PublicKeySource fromECPublicKey(ECPublicKey publicKey) {
            return new KeyObjectPublicKeySource(publicKey, this.validator);
        }
    }

}
