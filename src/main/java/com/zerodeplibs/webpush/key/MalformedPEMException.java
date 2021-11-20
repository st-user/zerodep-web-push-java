package com.zerodeplibs.webpush.key;

/**
 * This exception is thrown to indicate that an input text cannot be parsed as a valid PEM format.
 *
 * @author Tomoki Sato
 * @see PEMParser#parse(String)
 */
public class MalformedPEMException extends RuntimeException {

    MalformedPEMException(String message) {
        super(message);
    }

    MalformedPEMException(String message, Throwable cause) {
        super(message, cause);
    }

    static MalformedPEMException withDefaultMessage(Throwable cause) {
        return new MalformedPEMException(
            "An exception was thrown while parsing the input text. Please check the cause", cause);
    }
}
