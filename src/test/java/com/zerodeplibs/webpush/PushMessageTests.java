package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PushMessageTests {


    @Test
    public void shouldThrowExceptionWhenNullReferencesOrEmptyMessagesArePassed() {

        String messageFormat = "%s should not be null or empty.";

        assertThat(
            assertThrows(IllegalArgumentException.class, () -> PushMessage.of(null)).getMessage(),
            equalTo(String.format(messageFormat, "messageBytes")));

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> PushMessage.of(new byte[] {})).getMessage(),
            equalTo(String.format(messageFormat, "messageBytes")));

        assertThat(
            assertThrows(IllegalArgumentException.class, () -> PushMessage.ofUTF8(null)).getMessage(),
            equalTo(String.format(messageFormat, "messageText")));

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> PushMessage.ofUTF8("")).getMessage(),
            equalTo(String.format(messageFormat, "messageText")));
    }
}
