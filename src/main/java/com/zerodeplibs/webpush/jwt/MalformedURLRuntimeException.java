package com.zerodeplibs.webpush.jwt;

/**
 * A subclass of {@link RuntimeException} that wraps {@link java.net.MalformedURLException}.
 *
 * @author Tomoki Sato
 */
public class MalformedURLRuntimeException extends RuntimeException {
    MalformedURLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    static MalformedURLRuntimeException withDefaultMessage(Throwable cause) {
        return new MalformedURLRuntimeException(
            "An exception was thrown while parsing the input string. Please check the cause.",
            cause);
    }
}
