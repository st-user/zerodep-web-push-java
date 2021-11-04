package com.zerodeplibs.webpush.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.EncryptedPushMessage;
import com.zerodeplibs.webpush.header.Urgency;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class RequestPreparationInfoTests {


    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties() {

        PreparerBuilder.RequestPreparationInfo a =
            new PreparerBuilder.RequestPreparationInfo(
                "endpointUrl",
                "vapid",
                new TestingEncryptedPushMessage("data"),
                10L,
                Urgency.normal(),
                "MyTopic"
            );

        PreparerBuilder.RequestPreparationInfo b =
            new PreparerBuilder.RequestPreparationInfo(
                "endpointUrl",
                "vapid",
                new TestingEncryptedPushMessage("data"),
                10L,
                Urgency.normal(),
                "MyTopic"
            );

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));

        PreparerBuilder.RequestPreparationInfo empty_a =
            new PreparerBuilder.RequestPreparationInfo(
                "endpointUrl",
                "vapid",
                null,
                10L,
                Urgency.normal(),
                null
            );

        PreparerBuilder.RequestPreparationInfo empty_b =
            new PreparerBuilder.RequestPreparationInfo(
                "endpointUrl",
                "vapid",
                null,
                10L,
                Urgency.normal(),
                null
            );

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties() {

        PreparerBuilder.RequestPreparationInfo requestPreparationInfo =
            new PreparerBuilder.RequestPreparationInfo(
                "https://example.com",
                "vapid",
                new TestingEncryptedPushMessage("data"),
                10L,
                Urgency.normal(),
                "MyTopic"
            );

        assertThat(requestPreparationInfo.toString(), equalTo(
            "RequestPreparationInfo{"
                + "endpointUrl='https://example.com'"
                + ", vapidHeader='vapid'"
                + ", encryptedPushMessage=--TestingEncryptedPushMessage--"
                + ", ttl=10"
                + ", urgency='normal'"
                + ", topic='MyTopic'}"
        ));

        PreparerBuilder.RequestPreparationInfo requestPreparationInfoWithNull =
            new PreparerBuilder.RequestPreparationInfo(
                "https://example.com",
                "vapid",
                null,
                10L,
                Urgency.normal(),
                null
            );

        assertThat(requestPreparationInfoWithNull.toString(), equalTo(
            "RequestPreparationInfo{"
                + "endpointUrl='https://example.com'"
                + ", vapidHeader='vapid'"
                + ", ttl=10"
                + ", urgency='normal'}"
        ));
    }

    private static class TestingEncryptedPushMessage implements EncryptedPushMessage {

        private final byte[] b;

        TestingEncryptedPushMessage(String content) {
            this.b = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public byte[] toBytes() {
            return b;
        }

        @Override
        public int length() {
            return b.length;
        }

        @Override
        public String contentEncoding() {
            return "test-encoding";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestingEncryptedPushMessage that = (TestingEncryptedPushMessage) o;
            return Arrays.equals(b, that.b);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(b);
        }

        @Override
        public String toString() {
            return "--TestingEncryptedPushMessage--";
        }
    }
}
