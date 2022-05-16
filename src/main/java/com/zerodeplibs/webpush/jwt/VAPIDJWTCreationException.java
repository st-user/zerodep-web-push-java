package com.zerodeplibs.webpush.jwt;

/**
 * <p>
 * Wraps checked exceptions that may occur when a JWT for VAPID is being created.
 * </p>
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
     * Creates a new {@link VAPIDJWTCreationException} with the given message.
     *
     * @param message a message.
     */
    public VAPIDJWTCreationException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link VAPIDJWTCreationException} with the default message and the given cause.
     *
     * @param cause the underlying cause.
     * @return a new {@link VAPIDJWTCreationException}.
     */
    public static VAPIDJWTCreationException withDefaultMessage(Throwable cause) {
        return new VAPIDJWTCreationException(
            "An exception was thrown while creating a JWT. Please check the cause.", cause);
    }
}
