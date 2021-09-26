package com.zerodeplibs.webpush.key;

/**
 * This Exception is thrown by {@link PEMParser} during parsing
 * to indicate that the text being processed is malformed.
 *
 *
 * @author Tomoki Sato
 *
 * @see PEMParser#parse(String)
 */
public class InvalidPEMFormatException extends RuntimeException {

    InvalidPEMFormatException(String message) {
        super(message);
    }

    InvalidPEMFormatException(Throwable cause) {
        super(cause);
    }
}
