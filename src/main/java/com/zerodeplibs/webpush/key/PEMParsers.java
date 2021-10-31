package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;

/**
 * <p>
 * Static factory methods for {@link PEMParser}.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * The factory methods themselves are thread-safe.
 * The returned objects are also thread-safe because they are immutable.
 * </p>
 *
 * @author Tomoki Sato
 */
public class PEMParsers {

    private PEMParsers() {
    }

    /**
     * <p>
     * Creates a new {@link PEMParser} with the given label for encapsulation boundaries.
     * </p>
     *
     * <p>
     * This parser ignores characters outside the encapsulation boundaries
     * (i.e. all the characters before '-----BEGIN .... -----' or after '-----END ..... -----').
     * </p>
     *
     * <div><b>Examples:</b></div>
     * <pre>
     *  // Creates a parser for PEM-encoded texts
     *  // in which the private key data starts with '-----BEGIN PRIVATE KEY-----'
     *  // and ends with '-----END PRIVATE KEY-----'.
     *  PEMParser.ofStandard("PRIVATE KEY");
     *
     *  // Creates a parser for PEM-encoded texts
     *  // in which the public key data starts with '-----BEGIN PUBLIC KEY-----'
     *  // and ends with '-----END PUBLIC KEY-----'.
     *  PEMParser.ofStandard("PUBLIC KEY");
     * </pre>
     *
     * <p>
     * The returned {@link PEMParser} is intended to parse texts
     * encoded in <a href="https://datatracker.ietf.org/doc/html/rfc7468#section-3">the 'Standard' format described in RFC7468</a>.
     * </p>
     *
     * @param label a label for encapsulation boundaries.
     * @return a new {@link PEMParser}.
     * @throws IllegalArgumentException if the format of the given label is invalid.
     */
    public static PEMParser ofStandard(String label) {
        checkLabel(label);
        return new StandardPEMParser(label);
    }

    private static void checkLabel(String label) {
        WebPushPreConditions.checkNotNull(label, "label");

        char[] labelChars = label.toCharArray();

        boolean isCharBeforeSpOrMinus = false;
        for (int i = 0; i < labelChars.length; i++) {
            char current = labelChars[i];
            if (!isValidLabelChar(current)) {
                if (!isCharBeforeSpOrMinus && isSpaceOrMinus(current) && i != 0
                    && i != labelChars.length - 1) {
                    isCharBeforeSpOrMinus = true;
                    continue;
                }
                throw constructInvalidLabelException(label);
            }
            isCharBeforeSpOrMinus = false;

        }
    }

    private static IllegalArgumentException constructInvalidLabelException(String label) {
        return new IllegalArgumentException(String.format("The input label is invalid: %s", label));
    }

    private static boolean isValidLabelChar(char c) {
        return (0x21 <= c && c <= 0x2C) || (0x2E <= c && c <= 0x7E);
    }

    private static boolean isSpaceOrMinus(char c) {
        return c == '-' || c == ' ';
    }
}
