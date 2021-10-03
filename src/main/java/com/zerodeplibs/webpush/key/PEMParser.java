package com.zerodeplibs.webpush.key;

/**
 * Implementations of this interface provide the functionality for parsing PEM format.
 *
 * <p>
 * Usually, an instance of this interface
 * is obtained by a factory method of {@link PEMParsers}.
 * </p>
 *
 * <p>
 * <b>WARNING:</b>
 * Implementations of this interface are intended to parse PEM-encoded texts originated
 * from trusted sources(e.g. the PEM file created by yourself).
 * So don't parse data originated from untrusted sources.
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b>
 * Depends on the implementation. See {@link PEMParsers}.
 * </p>
 *
 * @author Tomoki Sato
 * @see PEMParsers
 */
public interface PEMParser {

    /**
     * The format string for encapsulation boundary(BEGIN).
     */
    String BEGIN_ENCAPSULATION_BOUNDARIES_FMR = "-----BEGIN %s-----";

    /**
     * The format string for encapsulation boundary(END).
     */
    String END_ENCAPSULATION_BOUNDARIES_FMR = "-----END %s-----";

    /**
     * The label in encapsulation boundary for PKCS #8 Private Key Info.
     */
    String PKCS8_PRIVATE_KEY_LABEL = "PRIVATE KEY";

    /**
     * The label in encapsulation boundary for Subject Public Key Info.
     */
    String SUBJECT_PUBLIC_KEY_INFO_LABEL = "PUBLIC KEY";

    /**
     * Parses the given PEM-encoded text and extracts its contents.
     *
     * @param pemText a PEM-encoded text
     * @return the contents extracted from the given PEM-encoded text.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     */
    byte[] parse(String pemText);
}
