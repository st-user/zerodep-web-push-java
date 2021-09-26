package com.zerodeplibs.webpush.key;

/**
 * This Exception is thrown by {@link PublicKeySource} during extraction
 * to indicate that the public key is invalid.
 *
 * @author Tomoki Sato
 *
 * @see PublicKeySource#extract()
 */
public class InvalidECPublicKeyException extends RuntimeException {

    InvalidECPublicKeyException(String message) {
        super(message);
    }
}
