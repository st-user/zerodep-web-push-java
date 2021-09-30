package com.zerodeplibs.webpush.jwt;

/**
 * A RuntimeException that wraps MalformedURLException.
 *
 * @author Tomoki Sato
 */
public class MalformedURLRuntimeException extends RuntimeException {
    MalformedURLRuntimeException(Throwable cause) {
        super(cause);
    }
}
