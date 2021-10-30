package com.zerodeplibs.webpush.jwt;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static com.zerodeplibs.webpush.TestAssertionUtil.assertStateCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class VAPIDJWTParamsTests {


    @Test
    public void shouldBeCreatedFromSpecifiedParams() {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam param = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com/resource")// Default port.
            .expiresAfterSeconds((int) TimeUnit.HOURS.toSeconds(24))
            .subject("mailto:example@example.com")
            .additionalClaim("hoge", "fuga")
            .build();

        assertThat(param.getOrigin(), equalTo("https://example.com"));
        assertThat(param.getExpiresAt(), equalTo(Date.from(mockForNow.plusSeconds(24 * 60 * 60))));
        assertThat(param.getSubject().get(), equalTo("mailto:example@example.com"));
        assertThat(param.getAdditionalClaim("hoge", String.class).get(), equalTo("fuga"));

        VAPIDJWTParam paramWithNotDefaultPort = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com:8080/resource")// Not default port.
            .expirationTime(Instant.now())
            .build();
        assertThat(paramWithNotDefaultPort.getOrigin(), equalTo("https://example.com:8080"));

        VAPIDJWTParam paramWithTimeUnit = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com/resource")
            .expiresAfter(12, TimeUnit.HOURS)
            .build();
        assertThat(paramWithTimeUnit.getExpiresAt(),
            equalTo(Date.from(mockForNow.plusSeconds(12 * 60 * 60))));
    }

    @Test
    public void shouldBeCreatedWithDefaultParams() {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam paramWithNotDefaultPort = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com/resource")
            .buildWithDefault();

        assertThat(paramWithNotDefaultPort.getExpirationTime(),
            equalTo(mockForNow.plus(3, ChronoUnit.MINUTES)));
    }

    @Test
    public void shouldBeCreatedFromSpecifiedRawParams() throws Exception {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam param = new TestingBuilder(mockForNow)
            .resourceURL(new URL("https://example.com/resource"))
            .expiresAt(Date.from(mockForNow.plusSeconds(24 * 60 * 60)))
            .build();

        assertThat(param.getOrigin(), equalTo("https://example.com"));
        assertThat(param.getExpiresAt(), equalTo(Date.from(mockForNow.plusSeconds(24 * 60 * 60))));

        VAPIDJWTParam paramWithExpirationTime = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com/resource")
            .expirationTime(mockForNow.plusSeconds(10))// Use #expirationTime
            .build();

        assertThat(paramWithExpirationTime.getExpiresAt(),
            equalTo(Date.from(mockForNow.plusSeconds(10))));
    }

    @Test
    public void builderShouldThrowExceptionWhenIllegalResourceURLsArePassed() {

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().resourceURLString(null),
            "resourceURLString");

        MalformedURLRuntimeException actualException =
            assertThrows(MalformedURLRuntimeException.class,
                () -> VAPIDJWTParam.getBuilder().resourceURLString("$$$$"));
        assertThat(actualException.getMessage(),
            equalTo(
                "An exception was thrown while parsing the input string. Please check the cause."));
        assertThat(actualException.getCause().getClass(), equalTo(MalformedURLException.class));

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().resourceURL(null),
            "resourceURL");
    }

    @Test
    public void builderShouldThrowExceptionWhenResourceURLIsSpecifiedMoreThanOnce() {

        String messageForMultipleCalls = "The methods for specifying "
            + "a resource URL(resourceURLString/resourceURL) cannot be called more than once.";

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .resourceURLString("http://example.com")
                .resourceURLString("http://example.com"),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .resourceURL(new URL("http://example.com"))
                .resourceURL(new URL("http://example.com")),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .resourceURLString("http://example.com")
                .resourceURL(new URL("http://example.com")),
            messageForMultipleCalls);
    }

    @Test
    public void builderShouldThrowExceptionWhenIllegalExpirationTimesArePassed() {

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().expiresAt(null),
            "expirationTime");

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().expiresAfter(1, null),
            "timeUnit");
    }

    @Test
    public void builderShouldThrowExceptionWhenExpirationTimeIsSpecifiedMoreThanOnce() {

        String messageForMultipleCalls = "The methods for specifying "
            +
            "expiration time(expiresAfter/expirationTime/expiresAfterSeconds/expiresAt) cannot be called more than once.";

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expiresAfterSeconds(1),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expirationTime(Instant.now())
                .expirationTime(Instant.now()),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAt(new Date())
                .expiresAt(new Date()),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expiresAt(new Date()),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expirationTime(Instant.now()),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expiresAfter(1, TimeUnit.HOURS),
            messageForMultipleCalls);
    }

    @Test
    public void shouldGetAdditionalClaimOnlyWhenValidNameAndTypeArePassed() {

        List<String> claim = new ArrayList<>();
        claim.add("hoge");
        VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(50)
            .additionalClaim("fuga", claim)
            .build();

        assertNullCheck(() -> param.getAdditionalClaim(null, String.class), "name");
        assertNullCheck(() -> param.getAdditionalClaim("fuga", null), "returnType");

        assertThat(param.getAdditionalClaim("fuga", List.class).get(), equalTo(claim));
        assertThat(param.getAdditionalClaim("fuga", ArrayList.class).get(), equalTo(claim));
        assertThat(param.getAdditionalClaim("fuga_", List.class).isPresent(), equalTo(false));
        assertThat(param.getAdditionalClaim("fuga", Set.class).isPresent(), equalTo(false));
    }

    @Test
    public void builderShouldThrowExceptionWhenIllegalSubjectsArePassed() {

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().subject(null),
            "subject");
    }

    @Test
    public void builderShouldThrowExceptionWhenIllegalAdditionalClaimsArePassed() {

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().additionalClaim(null, new Object()),
            "name");

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().additionalClaim("test", null),
            "value");
    }

    @Test
    public void builderShouldThrowExceptionWhenBuildingWithIllegalState() {

        String messageForResourceURLAbsence = "The resource URL isn't specified.";
        String messageForExpirationTimeAbsence = "The expiration time isn't specified.";

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .build(),
            messageForResourceURLAbsence);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .resourceURLString("http://example.com")
                .build(),
            messageForExpirationTimeAbsence);
    }

    @Test
    public void builderShouldThrowExceptionWhenReservedNameForAdditionalClaimIsGiven() {

        String messageForAud =
            "The \"aud\" claim should be specified via #resourceURL or #resourceURLString.";
        String messageForExp =
            "The \"exp\" claim should be specified via #expiresAt or #expiresAfter.";
        String messageForSub = "The \"sub\" claim should be specified via #subject.";

        VAPIDJWTParam.Builder param = VAPIDJWTParam.getBuilder();

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> param.additionalClaim("aud", "X")).getMessage(),
            equalTo(messageForAud));

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> param.additionalClaim("exp", "X")).getMessage(),
            equalTo(messageForExp));

        assertThat(
            assertThrows(IllegalArgumentException.class,
                () -> param.additionalClaim("sub", "X")).getMessage(),
            equalTo(messageForSub));
    }

    @Test
    public void twoObjectsShouldBeComparedWithEachOtherBasedOnTheirProperties() {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam a = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(100)
            .subject("SUBJECT")
            .additionalClaim("a", "b")
            .build();

        VAPIDJWTParam b = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(100)
            .subject("SUBJECT")
            .additionalClaim("a", "b")
            .build();

        assertThat(a.equals(null), equalTo(false));
        assertThat(a.equals(new Object()), equalTo(false));

        assertThat(a.equals(b), equalTo(true));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void toStringShouldReturnDescriptionBasedOnProperties() {

        Instant mockForNow = Instant.now();

        VAPIDJWTParam param = new TestingBuilder(mockForNow)
            .resourceURLString("https://example.com")
            .expiresAfterSeconds(100)
            .subject("SUBJECT")
            .additionalClaim("a", "b")
            .build();

        Map<String, String> claims = new HashMap<>();
        claims.put("a", "b");
        assertThat(param.toString(), equalTo(
            "VAPIDJWTParam{"
                + "origin='https://example.com'"
                + ", expirationTime=" + mockForNow.plusSeconds(100)
                + ", subject='SUBJECT'"
                + ", additionalClaims=" + claims + "}"
        ));

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
