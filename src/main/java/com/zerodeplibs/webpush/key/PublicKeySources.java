package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.interfaces.ECPublicKey;

/**
 * <p>
 * Static factory methods used to create instances of {@link PublicKeySource}.
 * </p>
 *
 * <p>
 * {@link PublicKeySource}s created by the static factory methods
 * of this class perform public key validation.
 * They use the validation method described in Section 5.6.2.3 of <a href="https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-56Ar2.pdf">Recommendation for Pair-Wise Key Establishment Schemes Using Discrete Logarithm Cryptography - NIST Special Publication 800-56A Revision 2</a>.
 * </p>
 *
 * <div><b>Examples:</b></div>
 * <p>
 * The following is an example of commands that uses OpenSSL
 * to generate a file that can be handled with {@link PublicKeySource}s.
 * </p>
 * <pre class="code">
 * openssl ecparam -genkey -name prime256v1 -noout -out my-private.pem
 * openssl ec -in my-private.pem -pubout -conv_form uncompressed -out my-pub.pem
 * </pre>
 *
 * <p>
 * If you want to generate in DER format, you can also do as follows.
 * </p>
 * <pre class="code">
 * openssl ec -in my-private.pem -pubout -conv_form uncompressed -outform der -out my-pub.der
 * </pre>
 *
 * <p>
 * The examples of using these files to create {@link PublicKeySource}s are as follows.
 * </p>
 * <pre class="code">
 * Path pemPath = new File("my-pub.pem").toPath();
 * Path derPath = new File("my-pub.der").toPath();
 *
 * PublicKeySource pemSource = PublicKeySources.ofPEMFile(pemPath);
 * PublicKeySource derSource = PublicKeySources.ofDERFile(derPath);
 *
 * byte[] pemBytes = Files.readAllBytes(pemPath);
 * String pemText = new String(pemBytes, StandardCharsets.UTF_8);
 * PublicKeySource pemSource2 = PublicKeySources.ofPEMText(pemText);
 *
 * byte[] derBytes = Files.readAllBytes(derPath);
 * PublicKeySource derSource2 = PublicKeySources.ofX509Bytes(derBytes);
 *
 * </pre>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Instances obtained through a factory method of this class are <b>NOT</b> thread-safe.
 * </p>
 *
 * @author Tomoki Sato
 * @see PublicKeySource
 * @see InvalidECPublicKeyException
 * @see PrivateKeySource
 * @see PrivateKeySources
 */
public class PublicKeySources {

    private PublicKeySources() {
    }

    /**
     * Create a new {@link PublicKeySource} with the given octet sequence representing
     * a public key on the P-256 curve encoded in the uncompressed form[X9.62].
     *
     * @param uncompressedBytes the octet sequence representing a public key.
     * @return a new {@link PublicKeySource}.
     * @throws MalformedUncompressedBytesException if the given octet sequence
     *                                             doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     */
    public static PublicKeySource ofUncompressedBytes(byte[] uncompressedBytes) {
        return BytesPublicKeySource.ofUncompressed(uncompressedBytes,
            ECPublicKeyUtil::validateECPublicKey);
    }

    /**
     * Creates a new {@link PublicKeySource} with the given octet sequence
     * that is assumed to be encoded according to the X.509 standard.
     *
     * @param x509Bytes the octet sequence representing a public key.
     * @return a new {@link PublicKeySource}.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofX509Bytes(byte[] x509Bytes) {
        return BytesPublicKeySource.ofX509(x509Bytes, ECPublicKeyUtil::validateECPublicKey);
    }

    /**
     * <p>
     * Creates a new {@link PublicKeySource} with the given PEM-encoded text.
     * The underlying octet sequence is assumed to be encoded according to the X.509 standard.
     * </p>
     *
     * <p>
     * The PEM-encoded text is assumed to contain a public key data
     * that starts with '-----BEGIN PUBLIC KEY-----'
     * and ends with '-----END PUBLIC KEY-----'.
     * </p>
     *
     * @param pemText the PEM-encoded text representing a public key.
     * @return a new {@link PublicKeySource}.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofPEMText(String pemText) {
        return ofPEMText(pemText,
            PEMParsers.ofStandard(PEMParser.SUBJECT_PUBLIC_KEY_INFO_LABEL));
    }

    /**
     * <p>
     * Creates a new {@link PublicKeySource} with the given PEM-encoded text
     * and the given {@link PEMParser}.
     * The underlying octet sequence is assumed to be encoded according to the X.509 standard.
     * </p>
     *
     * <p>
     * The PEM-encoded text is parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param pemText the PEM-encoded text representing a public key.
     * @param parser  the parser used to parse the PEM-encoded text.
     * @return a new {@link PublicKeySource}.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     * @see java.security.spec.X509EncodedKeySpec
     */
    public static PublicKeySource ofPEMText(String pemText, PEMParser parser) {
        WebPushPreConditions.checkNotNull(parser, "parser");
        return ofX509Bytes(parser.parse(pemText));
    }

    /**
     * <p>
     * Creates a new {@link PublicKeySource} with the PEM formatted file specified
     * by the given path.
     * The underlying octet sequence is assumed to be encoded according to the X.509 standard.
     * </p>
     *
     * <p>
     * The PEM formatted file is assumed to contain a public key data
     * that starts with '-----BEGIN PUBLIC KEY-----'
     * and ends with '-----END PUBLIC KEY-----'.
     * </p>
     *
     * @param path the path to a PEM formatted file.
     * @return a new {@link PublicKeySource}.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the content of the given file
     *                               cannot be parsed as a valid PEM format.
     */
    public static PublicKeySource ofPEMFile(Path path) throws IOException {
        return getPEMFileSourceBuilder(path).build();
    }

    /**
     * <p>
     * Creates a new {@link PublicKeySource} with the PEM formatted file specified
     * by the given path.
     * The underlying octet sequence is assumed to be encoded according to the X.509 standard.
     * </p>
     *
     * <p>
     * The content of the PEM file is parsed by the given {@link PEMParser}.
     * </p>
     *
     * @param path   the path to a PEM formatted file.
     * @param parser the parser used to parse the content of the PEM file.
     * @return a new {@link PublicKeySource}.
     * @throws IOException           if an I/O error occurs.
     * @throws MalformedPEMException if the content of the given file
     *                               cannot be parsed as a valid PEM format.
     */
    public static PublicKeySource ofPEMFile(Path path, PEMParser parser) throws IOException {
        return getPEMFileSourceBuilder(path).parser(parser).build();
    }

    /**
     * Creates a new {@link PublicKeySource} with the DER file specified by the given path.
     * Its octet sequence is assumed to be encoded according to the X.509 standard.
     *
     * @param path the path to a DER file.
     * @return a new {@link PublicKeySource}.
     * @throws IOException if an I/O error occurs.
     */
    public static PublicKeySource ofDERFile(Path path) throws IOException {
        return ofX509Bytes(FileUtil.readAllBytes(path));
    }

    /**
     * Creates a new {@link PublicKeySource} that wraps the given {@link ECPublicKey} object.
     *
     * @param publicKey an {@link ECPublicKey} object.
     * @return a new {@link PublicKeySource}.
     */
    public static PublicKeySource ofECPublicKey(ECPublicKey publicKey) {
        return new KeyObjectPublicKeySource(publicKey, ECPublicKeyUtil::validateECPublicKey);
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
     * The builder class for creating an instance of {@link PublicKeySource}
     * from a PEM formatted file.
     *
     * @author Tomoki Sato
     */
    public static class PEMFileSourceBuilder {
        private final Path path;
        private Charset charset = StandardCharsets.UTF_8;
        private PEMParser parser = PEMParsers.ofStandard(PEMParser.SUBJECT_PUBLIC_KEY_INFO_LABEL);

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
         * Specifies a parser used to parse the content of the PEM file.
         *
         * @param parser a parser used to parse the content of the PEM file.
         * @return this object.
         */
        public PEMFileSourceBuilder parser(PEMParser parser) {
            WebPushPreConditions.checkNotNull(parser, "parser");
            this.parser = parser;
            return this;
        }

        /**
         * Creates a new {@link PublicKeySource}.
         *
         * @return a new {@link PublicKeySource}.
         * @throws IOException           if an I/O error occurs.
         * @throws MalformedPEMException if the content of the file
         *                               cannot be parsed as a valid PEM format.
         */
        public PublicKeySource build() throws IOException {
            byte[] parsed = this.parser.parse(FileUtil.readAsString(path, charset));
            return ofX509Bytes(parsed);
        }

    }


}
