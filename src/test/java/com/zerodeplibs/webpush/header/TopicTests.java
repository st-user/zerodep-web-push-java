package com.zerodeplibs.webpush.header;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class TopicTests {

    @Test
    public void topicShouldThrowExceptionWhenIllegalCharactersArePassed() {
        Topic.ensure("AZaz09_-");
        Topic.ensure("--__9900zzaaZZAA");
        Topic.ensure(repeatChar("a", 32));

        String expectedMessage =
            "The Topic header field must be no more than 32 characters from the URL and a filename-safe Base 64 alphabet";


        assertThat(
            assertThrows(IllegalArgumentException.class, () -> Topic.ensure("")).getMessage(),
            equalTo(expectedMessage));

        assertThat(
            assertThrows(IllegalArgumentException.class, () -> Topic.ensure("/ABC")).getMessage(),
            equalTo(expectedMessage));

        assertThat(
            assertThrows(IllegalArgumentException.class, () -> Topic.ensure("012+")).getMessage(),
            equalTo(expectedMessage));

        assertThat(
            assertThrows(IllegalArgumentException.class, () -> Topic.ensure(repeatChar("a", 33))).getMessage(),
            equalTo(expectedMessage));
    }

    public String repeatChar(String s, int count) {
        return Stream.generate(() -> s).limit(count).collect(Collectors.joining());
    }
}
