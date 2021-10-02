package com.zerodeplibs.webpush.key;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.util.Base64;

/**
 * An implementation of the PEMParser
 * for <a href="https://datatracker.ietf.org/doc/html/rfc7468#section-3">the standard format described in RFC7468</a>.
 *
 * @author Tomoki Sato
 */
class StandardPEMParser implements PEMParser {

    private static final char[] END_EB_PREF = "-----END".toCharArray();
    private final String beginBoundary;
    private final String endBoundary;

    StandardPEMParser(String label) {
        this.beginBoundary = String.format(BEGIN_ENCAPSULATION_BOUNDARIES_FMR, label);
        this.endBoundary = String.format(END_ENCAPSULATION_BOUNDARIES_FMR, label);
    }

    @Override
    public byte[] parse(String pemText) {
        WebPushPreConditions.checkNotNull(pemText, "pemText");

        String contentLf = pemText.replace("\r\n", "\n");
        contentLf = contentLf.replace("\r", "\n");
        char[] contentArray = contentLf.toCharArray();
        char[] beginBoundaryChars = beginBoundary.toCharArray();
        char[] endBoundaryChars = endBoundary.toCharArray();

        StringBuilder base64Content = new StringBuilder();
        int skipEndIndex = -1;
        int padCount = 0;
        int lineNo = 1;
        boolean preebMatches = false;
        boolean preebLineEnded = false;
        boolean base64TextStarted = false;
        boolean base64LineStarted = false;
        boolean base64PadStarted = false;
        boolean postebStarted = false;
        boolean postebMatches = false;


        for (int i = 0; i < contentArray.length; i++) {

            char current = contentArray[i];

            if (isLf(current)) {
                lineNo++;
            }

            if (i <= skipEndIndex) {
                continue;
            }

            if (!preebMatches) {
                skipEndIndex = containsAnotherSequence(contentArray, beginBoundaryChars, i);
                preebMatches = skipEndIndex >= 0;
                continue;
            }

            if (!preebLineEnded) {
                if (isLf(current)) {
                    preebLineEnded = true;
                    continue;
                }
                if (isWsp(current)) {
                    continue;
                }
                throw new MalformedPEMException(
                    String.format("The line of the '-----BEGIN' encapsulation boundary "
                            + "contains an illegal character after '%s': %s", beginBoundary,
                        current)
                );
            }

            if (!base64TextStarted) { // handles *eolWSP
                if (isWsp(current) || isLf(current)) {
                    continue;
                }
                if (isBase64Char(current)) {
                    base64Content.append(current);
                    base64TextStarted = true;
                    base64LineStarted = true;
                    continue;
                }

                if (containsPostEb(contentArray, END_EB_PREF, i)) {
                    postebMatches = containsPostEb(contentArray, endBoundaryChars, i);
                    break;
                }

                throw constructInvalidBase64TextException(lineNo);
            }

            char beforeChar = contentArray[i - 1];
            if (!base64PadStarted && !postebStarted) {
                if (isBase64Char(current)) {
                    if (!base64LineStarted) {
                        throw constructInvalidBase64TextException(lineNo);
                    }
                    base64Content.append(current);
                    continue;
                }

                if (isWsp(current)) {
                    base64LineStarted = false;
                    continue;
                }

                if (isLf(current)) {
                    if (isLf(beforeChar) && !containsPostEb(contentArray, END_EB_PREF, i + 1)) {
                        throw constructInvalidBase64TextException(lineNo);
                    }
                    base64LineStarted = true;
                    continue;
                }

                if (base64LineStarted && isBase64Pad(current)) {
                    base64PadStarted = true;
                    padCount++;
                    base64Content.append(current);
                    continue;
                }

                if (containsPostEb(contentArray, END_EB_PREF, i)) {
                    postebMatches = containsPostEb(contentArray, endBoundaryChars, i);
                    break;
                }

                throw constructInvalidBase64TextException(lineNo);
            }

            if (!postebStarted) {
                if (isBase64Pad(current)) {
                    if (!base64LineStarted) {
                        throw constructInvalidBase64TextException(lineNo);
                    }
                    if (2 <= padCount) {
                        throw constructInvalidBase64TextException(lineNo);
                    }
                    padCount++;
                    base64Content.append(current);
                    continue;
                }

                if (isWsp(current)) {
                    base64LineStarted = false;
                    continue;
                }

                if (isLf(current)) {
                    if (isLf(beforeChar)) {
                        throw constructInvalidBase64TextException(lineNo - 1);
                    }
                    base64LineStarted = true;
                    continue;
                }

                if (containsPostEb(contentArray, END_EB_PREF, i)) {
                    postebMatches = containsPostEb(contentArray, endBoundaryChars, i);
                    break;
                }

                throw constructInvalidBase64TextException(lineNo);
            }

        }

        if (!preebMatches) {
            throw new MalformedPEMException(
                "A '-----BEGIN' encapsulation boundary doesn't exist or the format is invalid.");
        }

        if (!postebMatches) {
            throw new MalformedPEMException(
                "A '-----END' encapsulation boundary doesn't exist or the format is invalid.");
        }

        try {
            return Base64.getDecoder().decode(base64Content.toString());
        } catch (IllegalArgumentException e) {
            throw new MalformedPEMException(e);
        }
    }

    private MalformedPEMException constructInvalidBase64TextException(int lineNo) {
        String fmt = "The base64 text is malformed or contains an illegal character(line: %d).";
        return new MalformedPEMException(String.format(fmt, lineNo));
    }

    private boolean containsPostEb(char[] contentArray, char[] endBoundaryArray, int currentIndex) {
        char beforeChar = contentArray[currentIndex - 1];
        return isLf(beforeChar)
            && containsAnotherSequence(contentArray, endBoundaryArray, currentIndex) >= 0;
    }

    private int containsAnotherSequence(char[] contentArray, char[] another, int currentIndex) {

        int endIndex = -1;
        for (int i = 0; i < another.length; i++) {
            int contentIndex = i + currentIndex;
            if (contentArray.length <= contentIndex) {
                return -1;
            }
            if (contentArray[contentIndex] != another[i]) {
                return -1;
            }
            endIndex = contentIndex;
        }
        return endIndex;
    }

    private boolean isWsp(char c) {
        return c == 0x9 || c == 0x20;
    }

    private boolean isLf(char c) {
        return c == '\n';
    }

    private boolean isBase64Char(char c) {
        return c == '+' || c == '/' || isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isBase64Pad(char c) {
        return '=' == c;
    }
}
