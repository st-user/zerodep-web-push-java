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
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.InvalidECPublicKeyException;
import com.zerodeplibs.webpush.key.MalformedUncompressedBytesException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The builder class of "Preparer".
 *
 * <p>
 * "Preparer"s are the components which are used
 * for requesting the delivery of a push message.
 * </p>
 *
 * <p>
 * They help applications make an object which is used for such a request by:
 * </p>
 * <ul>
 * <li>extracting a URL of a push service</li>
 * <li>generating and setting proper HTTP header fields</li>
 * <li>encrypting a push message and setting it to the request body</li>
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
     * Creates a new "Preparer"
     * by constructing a {@link RequestPreparationInfo}
     * and then calling {@link #buildInternal(RequestPreparationInfo)}.
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
     * <li>The expiration time of the JWT(the "exp" claim): 3 minutes</li>
     * <li>The TTL header field: 24 hours</li>
     * <li>The Urgency header filed: "normal"</li>
     * </ul>
     *
     * @param vapidKeyPair a key pair used to sign the JWT for VAPID.
     * @return a new "Preparer".
     * @throws IllegalArgumentException            if the 'keys.p256dh' of the pushSubscription
     *                                             is invalid as a base64url string
     *                                             or the 'keys.auth'
     *                                             is invalid as a base64url string.
     * @throws IllegalStateException               if the pushSubscription isn't specified or
     *                                             the endpoint url of the pushSubscription
     *                                             is malformed.
     * @throws InvalidECPublicKeyException         if the public key extracted
     *                                             from the 'keys.p256dh' of the pushSubscription
     *                                             is invalid.
     * @throws MalformedUncompressedBytesException if the 'keys.p256dh' of the pushSubscription
     *                                             doesn't start with 0x04
     *                                             or the length isn't 65 bytes.
     */
    public T build(VAPIDKeyPair vapidKeyPair) {

        WebPushPreConditions.checkState(this.pushSubscription != null,
            "The pushSubscription isn't specified.");

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
     * Represents information which is required
     * for an application to request the delivery of a push message.
     *
     * <p>
     * Instances of this class are constructed(and therefore hold)
     * a push service url, values for HTTP header fields
     * and an encrypted request body(if a push message is specified).
     * </p>
     *
     * @author Tomoki Sato
     */
    protected static class RequestPreparationInfo {

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

        protected String getEndpointUrl() {
            return endpointUrl;
        }

        protected String getVapidHeader() {
            return vapidHeader;
        }

        protected Optional<EncryptedPushMessage> getEncryptedPushMessage() {
            return encryptedPushMessage;
        }

        protected String getTtlString() {
            return ttl.toString();
        }

        protected String getUrgency() {
            return urgency;
        }

        protected Optional<String> getTopic() {
            return topic;
        }
    }
}
