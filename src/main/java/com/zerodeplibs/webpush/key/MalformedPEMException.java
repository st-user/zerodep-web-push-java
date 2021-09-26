package com.zerodeplibs.webpush.key;

/**
 * This Exception is thrown by {@link PEMParser} during parsing
 * to indicate that the input text cannot be parsed as a valid PEM format.
 *
 * @author Tomoki Sato
 * @see PEMParser#parse(String)
 */
public class MalformedPEMException extends RuntimeException {

    MalformedPEMException(String message) {
        super(message);
    }

    MalformedPEMException(Throwable cause) {
        super(cause);
    }
}
