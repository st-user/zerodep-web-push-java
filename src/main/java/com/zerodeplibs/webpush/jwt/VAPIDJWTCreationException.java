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

    /**
     * Creates a new {@link VAPIDJWTCreationException} with the given message and cause.
     *
     * @param message a message.
     * @param cause   the underlying cause.
     */
    public VAPIDJWTCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new {@link VAPIDJWTCreationException} with the default message and the given cause.
     *
     * @param cause the underlying cause.
     */
    public static VAPIDJWTCreationException withDefaultMessage(Throwable cause) {
        return new VAPIDJWTCreationException(
            "An exception was thrown while creating a JWT. Please check the cause.", cause);
    }
}
