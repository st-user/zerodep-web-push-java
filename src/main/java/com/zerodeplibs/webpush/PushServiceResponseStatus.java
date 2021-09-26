package com.zerodeplibs.webpush;

/**
 *
 * <a href="https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol">Response from push service - The Web Push Protocol</a>
 */
public enum PushServiceResponseStatus {

    /**
     * <pre>
     * A 201 (Created) response indicates that the push message was
     * accepted.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5">5. Requesting Push Message Delivery</a>
     */
    CREATED(201, true, false, false),


    /**
     * <pre>
     * A push service MUST return a 400 (Bad Request) status code in
     * response to requests that omit the TTL header field.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.2">5.2. Push Message Time-To-Live</a>
     *
     * <pre>
     * Multiple values for the Urgency header field MUST NOT be included in
     * requests; otherwise, the push service MUST return a 400 (Bad Request)
     * status code.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.3">5.3. Push Message Urgency</a>
     *
     * <pre>
     * Topic header field that does not meet these constraints MUST return a
     * 400 (Bad Request) status code to the application server.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-5.4">5.4. Replacing Push Messages</a>
     *
     * <pre>
     * Though a push service is not obligated to
     * check either parameter for every push message, a push service SHOULD
     * reject push messages that have identical values for these parameters
     * with a 400 (Bad Request) status code.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8292#section-3.2">3.2. Public Key Parameter ("k")</a>
     */
    BAD_REQUEST(400),

    /**
     * <pre>
     * A push service MUST reject a message sent to a restricted push
     * message subscription if that message includes no "vapid"
     * authentication or invalid "vapid" authentication.  A 401
     * (Unauthorized) status code might be used if the authentication is
     * absent; a 403 (Forbidden) status code might be used if authentication
     * is invalid.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8292#section-4.2">4.2. Using Restricted Subscriptions</a>
     */
    UNAUTHORIZED(401),

    /**
     * <pre>
     * A push service MAY reject a
     * request with a 403 (Forbidden) status code [RFC7231] if the JWT
     * signature or its claims are invalid.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8292#section-2">2. Application Server Self-Identification</a>
     */
    FORBIDDEN(403),

    /**
     * <pre>
     * A push service MUST return a 404 (Not Found) status code if an
     * application server attempts to send a push message to an expired push
     * message subscription.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-7.3">7.3. Subscription Expiration</a>
     */
    NOT_FOUND(404, false, true, false),

    GONE(410, false, true, false),

    /**
     * <pre>
     * To limit the size of messages, the
     * push service MAY return a 413 (Payload Too Large) status code
     * [RFC7231] in response to requests that include an entity body that is
     * too large.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-7.2">7.2. Push Message Expiration</a>
     */
    PAYLOAD_TOO_LARGE(413),

    /**
     * <pre>
     * A push service MAY return a 429 (Too Many Requests) status code
     * [RFC6585] when an application server has exceeded its rate limit for
     * push message delivery to a push resource.  The push service SHOULD
     * also include a Retry-After header [RFC7231] to indicate how long the
     * application server is requested to wait before it makes another
     * request to the push resource.
     * </pre>
     * <a href="https://datatracker.ietf.org/doc/html/rfc8030#section-8.4">8.4. Denial-of-Service Considerations</a>
     */
    TOO_MANY_REQUEST(429, false, false, true)
    ;


    private final int statusCode;
    private final boolean isSuccess;
    private final boolean shouldRemoveSubscription;
    private final boolean shouldRetryLater;

    PushServiceResponseStatus(int statusCode, boolean isSuccess, boolean shouldRemoveSubscription, boolean shouldRetryLater) {
        this.statusCode = statusCode;
        this.isSuccess = isSuccess;
        this.shouldRemoveSubscription = shouldRemoveSubscription;
        this.shouldRetryLater = shouldRetryLater;
    }
    PushServiceResponseStatus(int statusCode) {
        this(statusCode, false, false, false);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean shouldRemoveSubscription() {
        return shouldRemoveSubscription;
    }

    public boolean shouldRetryLater() {
        return shouldRetryLater;
    }
}
