package com.zerodeplibs.webpush.key;

/**
 * This exception is thrown to indicate that the input octet sequence
 * for an EC public key isn't valid uncompressed form.
 *
 * @author Tomoki Sato
 */
public class MalformedUncompressedBytesException extends RuntimeException {
    MalformedUncompressedBytesException(String message) {
        super(message);
    }
}
