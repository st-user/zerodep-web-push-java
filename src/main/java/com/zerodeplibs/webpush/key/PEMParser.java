package com.zerodeplibs.webpush.key;

/**
 * Implementations of this interface provide the functionality for parsing PEM format.
 *
 * <p>
 * Usually, an instance of the implementation class of this interface
 * is obtained by the factory method of {@link PEMParsers}.
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b><br>
 * Depends on the implementation. See {@link PEMParsers}.
 * </p>
 *
 * @author Tomoki Sato
 * @see PEMParsers
 */
public interface PEMParser {

    String BEGIN_ENCAPSULATION_BOUNDARIES_FMR = "-----BEGIN %s-----";
    String END_ENCAPSULATION_BOUNDARIES_FMR = "-----END %s-----";

    String PKCS8_PRIVATE_KEY_LABEL = "PRIVATE KEY";
    String SUBJECT_PUBLIC_KEY_INFO_LABEL = "PUBLIC KEY";

    /**
     * Parses the given PEM-encoded text and extracts its contents.
     *
     * @param pemText a PEM-encoded text
     * @return the contents extracted from the given PEM-encoded text.
     * @throws MalformedPEMException if the given text
     *                               cannot be parsed as a valid PEM format.
     */
    byte[] parse(String pemText) throws MalformedPEMException;
}
