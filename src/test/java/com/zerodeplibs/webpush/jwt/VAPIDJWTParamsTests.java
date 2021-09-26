package com.zerodeplibs.webpush.jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class VAPIDJWTParamsTests {


    @Test
    public void jwtParamShouldBeCreatedFromSpecifiedParams() {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam param = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com/resource")
            .expiresAfterSeconds((int)TimeUnit.HOURS.toSeconds(24))
            .subject("mailto:example@example.com")
            .additionalClaim("hoge", "fuga")
            .build();

        assertThat(param.getOrigin(), equalTo("https://example.com"));
        assertThat(param.getExpiresAt(), equalTo(Date.from(mockForNow.plusSeconds(24 * 60 * 60))));
        assertThat(param.getSubject().get(), equalTo("mailto:example@example.com"));
        assertThat(param.getAdditionalClaim("hoge", String.class).get(), equalTo("fuga"));

    }

    @Test
    public void jwtParamShouldBeCreatedFromSpecifiedRawParams() throws Exception {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam param = new TestingBuilder(mockForNow)
            .resourceURL(new URL("https://example.com/resource"))
            .expiresAt(Date.from(mockForNow.plusSeconds(24 * 60 * 60)))
            .build();

        assertThat(param.getOrigin(), equalTo("https://example.com"));
        assertThat(param.getExpiresAt(), equalTo(Date.from(mockForNow.plusSeconds(24 * 60 * 60))));
    }

    private static class TestingBuilder extends VAPIDJWTParam.Builder {

        final Instant now;

        public TestingBuilder(Instant now) {
            this.now = now;
        }

        @Override
        Instant now() {
            return now;
        }
    }
}
