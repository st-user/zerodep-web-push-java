package com.zerodeplibs.webpush.jwt;

/**
 * Wraps checked exceptions during JWT creation for VAPID.
 *
 * <p>
 * Typically, this exception is thrown by an implementation of {@link VAPIDJWTGenerator}
 * provided by a sub-module for it.
 * </p>
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link #getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 * @see VAPIDJWTGenerator
 * @see VAPIDJWTGeneratorFactory
 */
public class VAPIDJWTCreationException extends RuntimeException {
    public VAPIDJWTCreationException(Throwable cause) {
        super(cause);
    }
}
