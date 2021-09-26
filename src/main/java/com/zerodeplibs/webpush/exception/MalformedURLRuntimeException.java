package com.zerodeplibs.webpush.exception;

/**
 * A RuntimeException that wraps MalformedURLException.
 *
 * @author Tomoki Sato
 */
public class MalformedURLRuntimeException extends RuntimeException {

    /**
     * Creates a MalformedURLRuntimeException with the given cause.
     *
     * @param cause a cause(usually {@link java.net.MalformedURLException}).
     */
    public MalformedURLRuntimeException(Throwable cause) {
        super(cause);
    }
}
