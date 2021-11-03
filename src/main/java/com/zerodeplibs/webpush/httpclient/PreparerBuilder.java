package com.zerodeplibs.webpush.httpclient;

import com.zerodeplibs.webpush.EncryptedPushMessage;
import com.zerodeplibs.webpush.MessageEncryption;
import com.zerodeplibs.webpush.MessageEncryptions;
import com.zerodeplibs.webpush.PushMessage;
import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.UserAgentMessageEncryptionKeyInfo;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.internal.WebPushPreConditions;
import com.zerodeplibs.webpush.jwt.MalformedURLRuntimeException;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.MalformedUncompressedBytesException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * The builder class of "Preparer".
 * </p>
 *
 * <p>
 * "Preparer"s are the components which are used
 * for requesting the delivery of a push message.
 * </p>
 *
 * <p>
 * In order to help applications make an object which is used for such a request,
 * "Preparer"s
 * </p>
 * <ul>
 * <li>extract a URL of a push service from a push subscription,</li>
 * <li>generate and set proper HTTP header fields and</li>
 * <li>encrypt a push message and set it to the request body.</li>
 * </ul>
 *
 * @param <T> the type of "Preparer" which is build by this builder instance.
 * @author Tomoki Sato
 */
public abstract class PreparerBuilder<T> {

    private PushSubscription pushSubscription;
    private final VAPIDJWTParam.Builder vapidJWTParamBuilder = VAPIDJWTParam.getBuilder();
    private PushMessage pushMessage;
    private Long ttl;
    private String urgency;
    private String topic;

    /**
     * Specifies a {@link PushSubscription}.
     *
     * @param pushSubscription a push subscription.
     * @return this object.
     * @throws NullPointerException if one of the push subscription's
     *                              fields(other than expirationTime) is null.
     */
    public PreparerBuilder<T> pushSubscription(PushSubscription pushSubscription) {

        WebPushPreConditions.checkNotNull(pushSubscription, "pushSubscription");
        WebPushPreConditions.checkNotNull(pushSubscription.getEndpoint(),
            "pushSubscription.endpoint");
        WebPushPreConditions.checkNotNull(pushSubscription.getKeys(),
            "pushSubscription.keys");
        WebPushPreConditions.checkNotNull(pushSubscription.getKeys().getP256dh(),
            "pushSubscription.keys.p256dh");
        WebPushPreConditions.checkNotNull(pushSubscription.getKeys().getAuth(),
            "pushSubscription.keys.auth");

        this.pushSubscription = pushSubscription;
        return this;
    }

    /**
     * Specifies the time after which a JWT for VAPID expires.
     *
     * @param expiresAfter the time after which a JWT expires.
     * @param timeUnit     the unit of the given <code>expiresAfter</code>.
     * @return this object.
     * @throws IllegalStateException if the methods for specifying expiration time
     *                               is called more than once.
     * @see VAPIDJWTParam.Builder#expiresAfter(int, TimeUnit)
     */
    public PreparerBuilder<T> vapidJWTExpiresAfter(int expiresAfter, TimeUnit timeUnit) {
        this.vapidJWTParamBuilder.expiresAfter(expiresAfter, timeUnit);
        return this;
    }

    /**
     * Specifies the time at which a JWT expires.
     *
     * @param expirationTime the time at which a JWT expires.
     * @return this object.
     * @throws IllegalStateException if the methods for specifying expiration time
     *                               is called more than once.
     * @see VAPIDJWTParam.Builder#expirationTime(Instant)
     */
    public PreparerBuilder<T> vapidJWTExpirationTime(Instant expirationTime) {
        this.vapidJWTParamBuilder.expirationTime(expirationTime);
        return this;
    }

    /**
     * Specifies a subject.
     *
     * @param subject a subject.
     * @return this object.
     * @see VAPIDJWTParam.Builder#subject(String)
     */
    public PreparerBuilder<T> vapidJWTSubject(String subject) {
        this.vapidJWTParamBuilder.subject(subject);
        return this;
    }

    /**
     * Specifies an additional claim.
     *
     * @param name  the name of an additional claim.
     * @param value the value of an additional claim.
     * @return this object.
     * @throws IllegalArgumentException if one of the "reserved" names is given.
     * @see VAPIDJWTParam.Builder#additionalClaim(String, Object)
     */
    public PreparerBuilder<T> vapidJWTAdditionalClaim(String name, String value) {
        this.vapidJWTParamBuilder.additionalClaim(name, value);
        return this;
    }

    /**
     * Specifies a push message.
     *
     * @param messageBytes the octet sequence representing a push message.
     * @return this object.
     * @throws IllegalArgumentException if the given octet sequence is null or empty.
     * @see PushMessage#of(byte[])
     */
    public PreparerBuilder<T> pushMessage(byte[] messageBytes) {
        this.pushMessage = PushMessage.of(messageBytes);
        return this;
    }

    /**
     * Specifies a push message.
     * The given text is encoded by using UTF-8.
     *
     * @param messageText the text representing a push message.
     * @return this object.
     * @throws IllegalArgumentException if the given text is null or empty.
     * @see PushMessage#ofUTF8(String)
     */
    public PreparerBuilder<T> pushMessage(String messageText) {
        this.pushMessage = PushMessage.ofUTF8(messageText);
        return this;
    }

    /**
     * Specifies a value for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a> header field
     * with a <code>timeUnit</code>.
     *
     * @param ttl      a time duration for the TTL header field in the given <code>timeUnit</code>.
     * @param timeUnit the unit of the <code>ttl</code> argument.
     * @return this object.
     * @throws IllegalArgumentException if the <code>ttl</code> is negative.
     * @see TTL
     */
    public PreparerBuilder<T> ttl(long ttl, TimeUnit timeUnit) {
        WebPushPreConditions.checkNotNull(timeUnit, "timeUnit");
        this.ttl = TTL.seconds(timeUnit.toSeconds(ttl));
        return this;
    }

    /**
     * Specifies a value for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @param urgencyOption an urgency value for the Urgency header field.
     * @return this object.
     * @see Urgency
     * @see #urgencyHigh()
     * @see #urgencyNormal()
     * @see #urgencyLow()
     * @see #urgencyVeryLow()
     */
    public PreparerBuilder<T> urgency(Urgency.UrgencyOption urgencyOption) {
        WebPushPreConditions.checkNotNull(urgencyOption, "urgencyOption");
        this.urgency = urgencyOption.getValue();
        return this;
    }

    /**
     * Specifies the "high" urgency for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @return this object.
     * @see Urgency
     */
    public PreparerBuilder<T> urgencyHigh() {
        this.urgency(Urgency.UrgencyOption.HIGH);
        return this;
    }

    /**
     * Specifies the "normal" urgency for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @return this object.
     * @see Urgency
     */
    public PreparerBuilder<T> urgencyNormal() {
        this.urgency(Urgency.UrgencyOption.NORMAL);
        return this;
    }

    /**
     * Specifies the "low" urgency for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @return this object.
     * @see Urgency
     */
    public PreparerBuilder<T> urgencyLow() {
        this.urgency(Urgency.UrgencyOption.LOW);
        return this;
    }

    /**
     * Specifies the "very-low" urgency for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">Urgency</a> header field.
     *
     * @return this object.
     * @see Urgency
     */
    public PreparerBuilder<T> urgencyVeryLow() {
        this.urgency(Urgency.UrgencyOption.VERY_LOW);
        return this;
    }

    /**
     * Specifies a value for the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.4">Topic</a> header field.
     *
     * @param topic a topic.
     * @return this object
     * @throws IllegalArgumentException if the given topic doesn't meet the constraints.
     * @see Topic
     */
    public PreparerBuilder<T> topic(String topic) {
        this.topic = Topic.ensure(topic);
        return this;
    }

    /**
     * <p>
     * Creates a new "Preparer"
     * by constructing a {@link RequestPreparationInfo}
     * and then calling {@link #buildInternal(RequestPreparationInfo)}.
     * </p>
     *
     * <p>
     * To do that, this method internally
     * </p>
     * <ul>
     * <li>generates a JWT for VAPID with the given <code>vapidKeyPair</code></li>
     * <li>performs message encryption(if the push message is specified)</li>
     * </ul>
     *
     * <p>
     * There is a default value for each of the following fields.
     * If the value isn't specified, the default value below is used.
     * </p>
     * <ul>
     * <li>The expiration time of the JWT for VAPID (the "exp" claim): 3 minutes</li>
     * <li>The TTL header field: 24 hours</li>
     * <li>The Urgency header filed: "normal"</li>
     * </ul>
     *
     * @param vapidKeyPair a key pair used to sign the JWT for VAPID.
     * @return a new "Preparer".
     * @throws IllegalArgumentException            if the 'keys.p256dh' of the push subscription
     *                                             is invalid as a base64url string
     *                                             or the 'keys.auth'
     *                                             is invalid as a base64url string.
     * @throws IllegalStateException               if the push subscription isn't specified.
     * @throws InvalidECPublicKeyException         if the public key extracted
     *                                             from the 'keys.p256dh' of the push subscription
     *                                             is invalid.
     * @throws MalformedUncompressedBytesException if the 'keys.p256dh' of the push subscription
     *                                             doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     * @throws MalformedURLRuntimeException        if the endpoint url of the push subscription
     *                                             is malformed.
     */
    public T build(VAPIDKeyPair vapidKeyPair) {

        WebPushPreConditions.checkState(this.pushSubscription != null,
            "The push subscription isn't specified.");

        setDefault();

        this.vapidJWTParamBuilder.resourceURLString(this.pushSubscription.getEndpoint());
        VAPIDJWTParam vapidjwtParam = this.vapidJWTParamBuilder.buildWithDefault();
        String credential = vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam);

        EncryptedPushMessage encryptedPushMessage = null;
        if (pushMessage != null) {
            MessageEncryption messageEncryption = MessageEncryptions.of();
            encryptedPushMessage = messageEncryption.encrypt(
                UserAgentMessageEncryptionKeyInfo.from(this.pushSubscription.getKeys()),
                this.pushMessage
            );
        }

        RequestPreparationInfo requestPreparationInfo = new RequestPreparationInfo(
            pushSubscription.getEndpoint(),
            credential,
            encryptedPushMessage,
            ttl,
            urgency,
            topic);


        return buildInternal(requestPreparationInfo);
    }

    private void setDefault() {
        if (this.ttl == null) {
            this.ttl = TimeUnit.DAYS.toSeconds(1);
        }
        if (this.urgency == null) {
            this.urgency = Urgency.normal();
        }
    }

    /**
     * Creates a new "Preparer" with the given <code>requestPreparationInfo</code>.
     *
     * @param requestPreparationInfo a requestPreparationInfo.
     * @return a new "Preparer".
     */
    protected abstract T buildInternal(RequestPreparationInfo requestPreparationInfo);

    /**
     * <p>
     * Represents information which is required
     * for an application to request the delivery of a push message.
     * </p>
     *
     * <p>
     * Instances of this class are constructed with (and therefore hold)
     * a push service url, values for HTTP header fields
     * and an encrypted request body(if a push message is specified).
     * </p>
     *
     * @author Tomoki Sato
     */
    public static class RequestPreparationInfo {

        private final String endpointUrl;
        private final String vapidHeader;
        private final Optional<EncryptedPushMessage> encryptedPushMessage;
        private final Long ttl;
        private final String urgency;
        private final Optional<String> topic;

        RequestPreparationInfo(String endpointUrl, String vapidHeader,
                               EncryptedPushMessage encryptedPushMessage, Long ttl,
                               String urgency, String topic) {
            this.endpointUrl = endpointUrl;
            this.vapidHeader = vapidHeader;
            this.encryptedPushMessage = Optional.ofNullable(encryptedPushMessage);
            this.ttl = ttl;
            this.urgency = urgency;
            this.topic = Optional.ofNullable(topic);
        }

        /**
         * Gets the endpoint url to which a push message is send.
         *
         * @return the endpoint url.
         */
        public String getEndpointUrl() {
            return endpointUrl;
        }

        /**
         * Gets the credential for VAPID.
         * The returned value should be set to the Authorization HTTP header field.
         *
         * @return the credential for VAPID.
         * @see VAPIDKeyPair#generateAuthorizationHeaderValue(VAPIDJWTParam)
         */
        public String getVapidHeader() {
            return vapidHeader;
        }

        /**
         * Gets the {@link EncryptedPushMessage}.
         * If a push message is specified at the time of the creation,
         * an Optional containing the {@link EncryptedPushMessage} is returned.
         *
         * @return an {@link Optional} that may or may not contain the {@link EncryptedPushMessage}.
         */
        public Optional<EncryptedPushMessage> getEncryptedPushMessage() {
            return encryptedPushMessage;
        }

        /**
         * Gets the value of <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a>.
         * The returned value should be set to the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a> HTTP header field.
         *
         * @return the value of <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">TTL</a>.
         */
        public String getTtlString() {
            return ttl.toString();
        }

        /**
         * Gets the value of Urgency.
         * The returned value should be set to the Urgency HTTP header field.
         *
         * @return the value of Urgency.
         */
        public String getUrgency() {
            return urgency;
        }

        /**
         * Gets the topic.
         * The value should be set to the <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.4">Topic</a> HTTP header field.
         * If a topic is specified at the time of the creation,
         * an Optional containing the topic is returned.
         *
         * @return an {@link Optional} that may or may not contain the topic.
         */
        public Optional<String> getTopic() {
            return topic;
        }
    }
}
