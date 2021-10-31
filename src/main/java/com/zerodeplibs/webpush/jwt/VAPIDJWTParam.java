package com.zerodeplibs.webpush.jwt;

import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * <p>
 * This class represents parameters for generating JSON Web Token (JWT) used
 * for the Voluntary Application Server Identification
 * (<a href="https://datatracker.ietf.org/doc/html/rfc8292">VAPID</a>).
 * </p>
 *
 * <div><b>Example:</b></div>
 * <pre class="code">
 *
 * VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
 *      .resourceURLString(subscription.getEndpoint())
 *      .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
 *      .subject("mailto:example@example.com")
 *      .build();
 *
 * </pre>
 *
 * @author Tomoki Sato
 * @see VAPIDJWTGenerator
 */
public class VAPIDJWTParam {

    private final String origin;
    private final Instant expirationTime;
    private final String subject;
    private final Map<String, Object> additionalClaims;

    private VAPIDJWTParam(String origin, Instant expirationTime, String subject,
                          Map<String, Object> additionalClaims) {
        this.origin = origin;
        this.expirationTime = expirationTime;
        this.subject = subject;
        this.additionalClaims = additionalClaims;
    }

    /**
     * Gets a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * <p>
     * Gets one of the additional claims specified at the time of the instantiation
     * by the given name and type.
     * </p>
     *
     * <div><b>Example:</b></div>
     *
     * <pre class="code">
     *
     * ArrayList&lt;String&gt; hogeList = new ArrayList&lt;&gt;();
     * VAPIDJWTParam param = VAPIDJWTParam.getBuilder()
     *      .resourceURLString(.....)
     *      .expiresAfterSeconds(.....)
     *      .additionalClaim("hoge", hogeList);
     *      .build();
     *
     *  param.getAdditionalClaim("fuga", Object.class); // -&gt; Optional.empty();
     *  param.getAdditionalClaim("hoge", String.class); // -&gt; Optional.empty();
     *  param.getAdditionalClaim("hoge", ArrayList.class).get(); // -&gt; hogeList;
     *  param.getAdditionalClaim("hoge", List.class).get(); // -&gt; hogeList;
     *
     * </pre>
     *
     * @param name       the name of the additional claim.
     * @param returnType the type of the additional claim.
     * @param <T>        the type of the additional claim.
     * @return an {@link Optional} that contains or doesn't contain the additional claim.
     * @see Builder#additionalClaim(String, Object)
     */
    public <T> Optional<T> getAdditionalClaim(String name, Class<T> returnType) {
        WebPushPreConditions.checkNotNull(name, "name");
        WebPushPreConditions.checkNotNull(returnType, "returnType");

        Object v = this.additionalClaims.get(name);
        if (returnType.isInstance(v)) {
            @SuppressWarnings("unchecked")
            T ret = (T) v;
            return Optional.of(ret);
        }
        return Optional.empty();
    }

    /**
     * Performs the given action for each additional claim.
     *
     * @param action the action to be performed for each additional claim.
     */
    public void forEachAdditionalClaim(BiConsumer<String, Object> action) {
        this.additionalClaims.forEach(action);
    }

    /**
     * Gets the additional claims.
     * The returned map is unmodifiable.
     *
     * @return the additional claims.
     */
    public Map<String, Object> getAdditionalClaims() {
        return this.additionalClaims;
    }

    /**
     * The builder class for {@link VAPIDJWTParam}.
     *
     * @author Tomoki Sato
     */
    public static class Builder {

        private static final Map<String, String> RESERVED_NAMES_WITH_MESSAGE =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("aud",
                        "The \"aud\" claim should be specified via "
                            + "#resourceURL or #resourceURLString.");
                    put("exp",
                        "The \"exp\" claim should be specified via "
                            + "#expiresAt or #expiresAfter.");
                    put("sub", "The \"sub\" claim should be specified via #subject.");
                }
            });

        private URL _resourceURL;
        private Instant _expirationTime;
        private String _subject;

        private final Map<String, Object> additionalClaims = new LinkedHashMap<>();

        private static final String MSG_RESOURCE_URL_NO_MORE_THAN_ONCE =
            "The methods for specifying "
                + "a resource URL(resourceURLString/resourceURL) cannot be called more than once.";

        private static final String MSG_EXPIRES_AT_NO_MORE_THAN_ONCE = "The methods for specifying "
            + "expiration time(expiresAfter/expirationTime/expiresAfterSeconds/expiresAt) "
            + "cannot be called more than once.";

        Builder() {
            // Should be accessed internally.
        }

        /**
         * <p>
         * Specifies a push resource URL from which the origin is extracted.
         * </p>
         *
         * <p>
         * Typically,
         * </p>
         * <ul>
         *  <li>the resource URL is the value obtained from <a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">an endpoint field of a push subscription</a>.</li>
         *  <li>the extracted origin is used as an "aud"(Audience) claim.</li>
         * </ul>
         *
         * @param resourceURLString the string representation of a push resource URL.
         * @return this object.
         * @throws MalformedURLRuntimeException if the underlying {@link URL#URL(String)}
         *                                      throws {@link MalformedURLException}.
         * @throws IllegalStateException        if the methods for specifying a resource URL
         *                                      is called more than once.
         */
        public Builder resourceURLString(String resourceURLString) {
            WebPushPreConditions.checkNotNull(resourceURLString, "resourceURLString");
            try {
                this.resourceURL(new URL(resourceURLString));
            } catch (MalformedURLException e) {
                throw MalformedURLRuntimeException.withDefaultMessage(e);
            }
            return this;
        }


        /**
         * <p>
         * Specifies a push resource URL from which the origin is extracted.
         * </p>
         *
         * <p>
         * Typically,
         * </p>
         * <ul>
         *  <li>the resource URL is the value obtained from <a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">an endpoint field of a push subscription</a>.</li>
         *  <li>the extracted origin is used as an "aud"(Audience) claim.</li>
         * </ul>
         *
         * @param resourceURL a push resource URL.
         * @return this object.
         * @throws IllegalStateException if the methods for specifying a resource URL
         *                               is called more than once.
         */
        public Builder resourceURL(URL resourceURL) {
            WebPushPreConditions.checkNotNull(resourceURL, "resourceURL");
            WebPushPreConditions.checkState(this._resourceURL == null,
                MSG_RESOURCE_URL_NO_MORE_THAN_ONCE);
            this._resourceURL = resourceURL;
            return this;
        }

        /**
         * <p>
         * Specifies the time after which a JWT expires.
         * </p>
         *
         * <p>
         * Typically, the specified expiration time is used as an "exp" (Expiry) claim.
         * </p>
         *
         * @param expiresAfter the time after which a JWT expires.
         * @param timeUnit     the unit of the given <code>expiresAfter</code>.
         * @return this object.
         * @throws IllegalStateException if the methods for specifying expiration time
         *                               is called more than once.
         */
        public Builder expiresAfter(int expiresAfter, TimeUnit timeUnit) {
            WebPushPreConditions.checkNotNull(timeUnit, "timeUnit");
            // TODO. from JDK9, we can use TimeUnit#toChronoUnit
            this.expirationTime(now().plusNanos(timeUnit.toNanos(expiresAfter)));
            return this;
        }

        /**
         * <p>
         * Specifies the time in seconds after which a JWT expires.
         * </p>
         *
         * <p>
         * Typically, the specified expiration time is used as an "exp" (Expiry) claim.
         * </p>
         *
         * @param seconds the time in seconds after which a JWT expires.
         * @return this object.
         * @throws IllegalStateException if the methods for specifying expiration time
         *                               is called more than once.
         * @deprecated Use {@link #expiresAfter(int, TimeUnit)}.
         */
        @Deprecated
        public Builder expiresAfterSeconds(int seconds) {
            // TODO check if no more than 24 hours
            this.expirationTime(now().plusSeconds(seconds));
            return this;
        }

        /**
         * <p>
         * Specifies the time at which a JWT expires.
         * </p>
         *
         * <p>
         * Typically, the specified expiration time is used as an "exp" (Expiry) claim.
         * </p>
         *
         * @param expirationTime the time at which a JWT expires.
         * @return this object.
         * @throws IllegalStateException if the methods for specifying expiration time
         *                               is called more than once.
         */
        public Builder expirationTime(Instant expirationTime) {
            WebPushPreConditions.checkNotNull(expirationTime, "expirationTime");
            WebPushPreConditions.checkState(this._expirationTime == null,
                MSG_EXPIRES_AT_NO_MORE_THAN_ONCE);
            this._expirationTime = expirationTime;
            return this;
        }

        /**
         * <p>
         * Specifies the time at which a JWT expires.
         * </p>
         *
         * <p>
         * Typically, the specified expiration time is used as an "exp" (Expiry) claim.
         * </p>
         *
         * @param expiresAt the time at which a JWT expires.
         * @return this object.
         * @throws IllegalStateException if the methods for specifying expiration time
         *                               is called more than once.
         * @deprecated Use {@link #expirationTime(Instant)}}.
         */
        @Deprecated
        public Builder expiresAt(Date expiresAt) {
            WebPushPreConditions.checkNotNull(expiresAt, "expirationTime");
            this.expirationTime(expiresAt.toInstant());
            return this;
        }

        /**
         * <p>
         * Specifies a subject.
         * </p>
         *
         * <p>
         * Typically, the specified subject is used as a "sub" (Subject) claim.
         * </p>
         *
         * @param subject a subject.
         * @return this object.
         */
        public Builder subject(String subject) {
            WebPushPreConditions.checkNotNull(subject, "subject");
            this._subject = subject;
            return this;
        }

        /**
         * <p>
         * Specifies an additional claim.
         * </p>
         *
         * <p>
         * Typically, the specified entry is used as a claim in a token's payload.
         * </p>
         *
         * <p>
         * The following names are "reserved".
         * So if one of these names is given, an {@link IllegalArgumentException} is thrown.
         * </p>
         * <ul>
         * <li>"aud" - this claim should be specified
         * via {@link #resourceURL(URL)} or {@link #resourceURLString(String)}.</li>
         * <li>"exp" - this claim should be specified
         * via {@link #expirationTime(Instant)} or {@link #expiresAfterSeconds(int)}.</li>
         * <li>"sub" - this claim should be specified
         * via {@link #subject(String)}</li>
         * </ul>
         *
         * @param name  the name of an additional claim.
         * @param value the value of an additional claim.
         * @return this object.
         * @throws IllegalArgumentException if one of the "reserved" names is given.
         */
        public Builder additionalClaim(String name, Object value) {
            WebPushPreConditions.checkNotNull(name, "name");
            WebPushPreConditions.checkNotNull(value, "value");

            String errorMessage = RESERVED_NAMES_WITH_MESSAGE.get(name);
            WebPushPreConditions.checkArgument(errorMessage == null, errorMessage);

            additionalClaims.put(name, value);
            return this;
        }

        /**
         * Creates a new {@link VAPIDJWTParam}.
         *
         * @return a new {@link VAPIDJWTParam}.
         * @throws IllegalStateException if the resource URL and
         *                               the expiration time aren't specified.
         */
        public VAPIDJWTParam build() {
            WebPushPreConditions.checkState(this._resourceURL != null,
                "The resource URL isn't specified.");
            WebPushPreConditions.checkState(this._expirationTime != null,
                "The expiration time isn't specified.");

            String origin = this._resourceURL.getProtocol() + "://" + this._resourceURL.getHost();
            // https://datatracker.ietf.org/doc/html/rfc6454#section-6.1
            int port = this._resourceURL.getPort();
            if (port != -1) {
                origin += ":";
                origin += String.valueOf(port);
            }
            return new VAPIDJWTParam(
                origin,
                _expirationTime,
                _subject,
                Collections.unmodifiableMap(this.additionalClaims));
        }

        /**
         * Creates a new {@link VAPIDJWTParam}.
         * If the expiration time isn't specified, the value is set to 3 minutes.
         *
         * @return a new {@link VAPIDJWTParam}.
         * @throws IllegalStateException if the resource URL isn't specified.
         */
        public VAPIDJWTParam buildWithDefault() {
            if (this._expirationTime == null) {
                this._expirationTime = now().plus(3, ChronoUnit.MINUTES);
            }
            return build();
        }

        // Visible for testing
        Instant now() {
            return Instant.now();
        }
    }

    /**
     * <p>
     * Gets the origin extracted from the resource URL.
     * </p>
     *
     * <p>
     * Typically, the returned value is set to an "aud" claim.
     * </p>
     *
     * @return the origin.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Gets the expiration time at which a JWT expires.
     *
     * @return the expiration time.
     */
    public Date getExpiresAt() {
        return Date.from(getExpirationTime());
    }

    /**
     * Gets the expiration time at which a JWT expires.
     *
     * @return the expiration time.
     */
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * <p>
     * Gets the number of seconds from January 1, 1970, 00:00:00 GMT
     * to the expiration time.
     * </p>
     *
     * <p>
     * Typically, the returned value is set to an "exp" claim.
     * </p>
     *
     * @return the number of seconds.
     */
    public long getExpiresAtInSeconds() {
        return getExpiresAt().getTime() / 1000;
    }

    /**
     * <p>
     * Gets the subject.
     * If a subject is specified at the time of the creation,
     * an Optional containing the subject is returned.
     * </p>
     *
     * <p>
     * Typically, the returned value is set to an "sub" claim.
     * </p>
     *
     * @return an {@link Optional} that contains or doesn't contain the subject.
     */
    public Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    /**
     * Compares the given object with this object based on their properties.
     *
     * @param o an object.
     * @return true if the given object is equal to this object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VAPIDJWTParam)) {
            return false;
        }
        VAPIDJWTParam that = (VAPIDJWTParam) o;
        return getOrigin().equals(that.getOrigin())
            && getExpirationTime().equals(that.getExpirationTime())
            && Objects.equals(getSubject(), that.getSubject())
            && additionalClaims.equals(that.additionalClaims);
    }

    /**
     * Returns the hash code value for this object based on its properties.
     *
     * @return the hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            getOrigin(),
            getExpirationTime(),
            getSubject(),
            additionalClaims);
    }

    @Override
    public String toString() {
        return "VAPIDJWTParam{"
            + "origin='" + origin + '\''
            + ", expirationTime=" + expirationTime
            + ", subject='" + subject + '\''
            + ", additionalClaims=" + additionalClaims
            + '}';
    }
}
