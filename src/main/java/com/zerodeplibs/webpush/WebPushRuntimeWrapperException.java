package com.zerodeplibs.webpush;

import java.security.InvalidAlgorithmParameterException;

/**
 * This runtime exception class is intended to wrap the checked exceptions
 * that users of this library usually do not need to handle explicitly.
 *
 * <p>
 * These are the checked exceptions during cryptographic processing,
 * such as {@link InvalidAlgorithmParameterException}.
 * </p>
 *
 * <p>
 * The underlying exception can be obtained
 * by calling {@link WebPushRuntimeWrapperException#getCause()}.
 * </p>
 *
 * @author Tomoki Sato
 */
public class WebPushRuntimeWrapperException extends RuntimeException {
    public WebPushRuntimeWrapperException(Throwable cause) {
        super(cause);
    }
}
