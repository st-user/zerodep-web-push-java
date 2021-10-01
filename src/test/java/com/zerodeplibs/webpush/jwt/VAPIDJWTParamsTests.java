package com.zerodeplibs.webpush.jwt;

import static com.zerodeplibs.webpush.TestAssertionUtil.assertNullCheck;
import static com.zerodeplibs.webpush.TestAssertionUtil.assertStateCheck;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            .expiresAfterSeconds(50)
            .build();
        assertThat(paramWithNotDefaultPort.getOrigin(), equalTo("https://example.com:8080"));
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
    }

    @Test
    public void builderShouldThrowExceptionWhenIllegalResourceURLsArePassed() {

        String messageForMultipleCalls = "The methods for specifying "
            + "a resource URL(resourceURLString/resourceURL) cannot be called more than once.";

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().resourceURLString(null),
            "resourceURLString");

        assertThat(assertThrows(MalformedURLRuntimeException.class,
                () -> VAPIDJWTParam.getBuilder().resourceURLString("$$$$")).getCause().getClass(),
            equalTo(
                MalformedURLException.class));

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().resourceURL(null),
            "resourceURL");

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

        String messageForMultipleCalls = "The methods for specifying "
            + "expiration time(expiresAfterSeconds/expiresAt) cannot be called more than once.";

        assertNullCheck(() -> VAPIDJWTParam.getBuilder().expiresAt(null),
            "expiresAt");

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expiresAfterSeconds(1),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAt(new Date())
                .expiresAt(new Date()),
            messageForMultipleCalls);

        assertStateCheck(() -> VAPIDJWTParam.getBuilder()
                .expiresAfterSeconds(1)
                .expiresAt(new Date()),
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
