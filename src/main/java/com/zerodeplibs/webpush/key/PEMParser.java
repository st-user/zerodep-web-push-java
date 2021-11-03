package com.zerodeplibs.webpush.key;

/**
 * <p>
 * Implementations of this interface provide the functionality for parsing PEM format.
 * </p>
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by using a factory method of {@link PEMParsers}.
 * </p>
 *
 * <div><b>WARNING:</b></div>
 * <p>
 * Implementations of this interface are intended to parse PEM-encoded texts originated
 * from trusted sources(e.g. a PEM file created by yourself).
 * So don't parse data originated from untrusted sources.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Depends on the implementation. See {@link PEMParsers}.
 * </p>
 *
 * @author Tomoki Sato
 * @see PEMParsers
 */
public interface PEMParser {

    /**
     * The format string used to make a pre-encapsulation boundary.
     */
    String BEGIN_ENCAPSULATION_BOUNDARIES_FMR = "-----BEGIN %s-----";

    /**
     * The format string used to make a post-encapsulation boundary.
     */
    String END_ENCAPSULATION_BOUNDARIES_FMR = "-----END %s-----";

    /**
     * The type label in an encapsulation boundary indicating
     * that the content is a PKCS #8 Private Key Info.
     */
    String PKCS8_PRIVATE_KEY_LABEL = "PRIVATE KEY";

    /**
     * The type label in an encapsulation boundary indicating
     * that the content is a Subject Public Key Info.
     */
    String SUBJECT_PUBLIC_KEY_INFO_LABEL = "PUBLIC KEY";

    /**
     * Parses the given PEM-encoded text and extracts its content.
     *
     * @param pemText a PEM-encoded text
     * @return the content extracted from the given PEM-encoded text.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     */
    byte[] parse(String pemText);
}
