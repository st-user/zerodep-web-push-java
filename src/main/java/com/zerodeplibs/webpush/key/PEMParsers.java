package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;

/**
 * Static utility methods for instantiating an implementation class of {@link PEMParser}.
 *
 * @author Tomoki Sato
 */
public class PEMParsers {

    private PEMParsers() {
    }

    /**
     * Creates an instance of the {@link PEMParser} implementation
     * with the given label for encapsulation boundaries.
     *
     * <p>
     * This parser ignores characters outside the encapsulation boundaries
     * (ie, characters before '-----BEGIN .... -----' or after '-----END ..... -----').
     * </p>
     *
     * <p>
     * <b>Examples:</b>
     * <pre>
     *  // for '-----BEGIN PRIVATE KEY-----'
     *  // and '-----END PRIVATE KEY-----'.
     *  PEMParser.ofStandard("PRIVATE KEY");
     *
     *  // for '-----BEGIN PUBLIC KEY-----'
     *  // and '-----END PUBLIC KEY-----'.
     *  PEMParser.ofStandard("PUBLIC KEY");
     * </pre>
     * </p>
     *
     * <p>
     * The returned PEMParser is intended to parse texts
     * encoded in <a href="https://datatracker.ietf.org/doc/html/rfc7468#section-3">the standard format described in RFC7468</a>.
     * </p>
     *
     * @param label a label for encapsulation boundaries.
     * @return an instance of the {@link PEMParser} implementation.
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
