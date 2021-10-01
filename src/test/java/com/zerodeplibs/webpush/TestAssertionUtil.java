package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

public interface TestAssertionUtil {

    String NULL_CHECK_FMT = "%s should not be null.";

    static String nullMsg(String name) {
        return String.format(NULL_CHECK_FMT, name);
    }

    static void assertNullCheck(Executable checker, String parameterName) {
        assertThat(assertThrows(NullPointerException.class, checker).getMessage(),
            equalTo(TestAssertionUtil.nullMsg(parameterName)));
    }

    static void assertStateCheck(Executable checker, String message) {
        assertThat(assertThrows(IllegalStateException.class, checker).getMessage(),
            equalTo(message));
    }
}
