package com.zerodeplibs.webpush.key;

/**
 * This exception is thrown to indicate that an input octet sequence
 * isn't valid uncompressed form[X9.62].
 *
 * @author Tomoki Sato
 */
public class MalformedUncompressedBytesException extends RuntimeException {
    MalformedUncompressedBytesException(String message) {
        super(message);
    }
}
