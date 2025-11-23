package com.zerodeplibs.webpush;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
            assertThrows(IllegalArgumentException.class,
                () -> PushMessage.ofUTF8(null)).getMessage(),
            equalTo(String.format(messageFormat, "messageText")));

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> PushMessage.ofUTF8("")).getMessage(),
            equalTo(String.format(messageFormat, "messageText")));
    }


    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties() {

        PushMessage a = PushMessage.ofUTF8("Hello");
        PushMessage b = PushMessage.ofUTF8("Hello");

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties() {

        PushMessage message = PushMessage.ofUTF8("Hello");

        assertThat(message.toString(), equalTo(
            "PushMessage{"
                + "message=" + Arrays.toString("Hello".getBytes(StandardCharsets.UTF_8))
                + "}"
        ));
    }
}
