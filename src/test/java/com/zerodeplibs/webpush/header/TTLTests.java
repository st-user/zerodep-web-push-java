package com.zerodeplibs.webpush.header;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TTLTests {

    @Test
    public void shouldSupplyAnIntegerRepresentingTimeInSeconds() {

        assertThat(TTL.days(2), equalTo(2 * 24 * 60 * 60L));
        assertThat(TTL.hours(2), equalTo(2 * 60 * 60L));
        assertThat(TTL.minutes(2), equalTo(2 * 60L));
        assertThat(TTL.seconds(2), equalTo(2L));
    }

    @Test
    public void shouldThrowExceptionWhenNegativeNumberIsPassed() {
        String expectedMessage =
            "TTL should be a non-negative number.";

        assertThat(assertThrows(IllegalArgumentException.class, () -> TTL.days(-1)).getMessage(),
            equalTo(expectedMessage));
        assertThat(assertThrows(IllegalArgumentException.class, () -> TTL.hours(-1)).getMessage(),
            equalTo(expectedMessage));
        assertThat(assertThrows(IllegalArgumentException.class, () -> TTL.minutes(-1)).getMessage(),
            equalTo(expectedMessage));
        assertThat(assertThrows(IllegalArgumentException.class, () -> TTL.seconds(-1)).getMessage(),
            equalTo(expectedMessage));
    }
}
