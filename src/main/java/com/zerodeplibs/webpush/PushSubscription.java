package com.zerodeplibs.webpush;

import java.util.Objects;

/**
 * <p>
 * This class represents a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a> described in the <a href="https://www.w3.org/TR/push-api/">Push API - W3C</a> specification.
 * </p>
 *
 * <p>
 * This class is used as if it was a simple "JavaBean"
 * (this class doesn't implement {@link java.io.Serializable}
 * and therefore doesn't conform to the "JavaBean" standard strictly).
 * </p>
 *
 * <p>
 * Typically, each value of the fields of this class is
 * set to the value of the corresponding field of a push subscription retrieved from a browser.
 * </p>
 *
 * <p>
 * The following is a browser-side javascript example
 * to get a JSON object of a push subscription.
 * </p>
 * <pre class="code">
 *
 *     // It is assumed that you have registered a service worker.
 *     const registration = await navigator.serviceWorker.ready;
 *     const subscription = await registration.pushManager.subscribe({
 *         userVisibleOnly: true,
 *         applicationServerKey: serverPublicKey // your server's public key.
 *     });
 *
 *     // Sends the subscription to your server.
 *     await fetch('/subscribe', {
 *         method: 'POST',
 *         body: JSON.stringify(subscription),
 *         headers: {
 *             'content-type': 'application/json'
 *         }
 *     });
 *
 * </pre>
 *
 * <p>
 * Instances of this class can be generated by utilizing a library
 * that provides functionalities of JSON such as <a href="https://github.com/FasterXML/jackson">jackson</a>.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Instances of this class are mutable.
 * So use them carefully if they are accessed from multiple threads.
 * </p>
 *
 * @author Tomoki Sato
 * @see UserAgentMessageEncryptionKeyInfo
 */
public class PushSubscription {

    private String endpoint;
    private Long expirationTime;
    private Keys keys;

    /**
     * Creates a new {@link PushSubscription}.
     */
    public PushSubscription() {
    }

    /**
     * Creates a new {@link PushSubscription}.
     * All the fields are deeply copied from <code>another</code>.
     *
     * @param another an object from which the fields are copied.
     */
    public PushSubscription(PushSubscription another) {
        this.endpoint = another.getEndpoint();
        this.expirationTime = another.getExpirationTime();
        this.keys = new Keys(another.keys);
    }

    /**
     * <p>
     * This class represents a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field for a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>.
     * </p>
     *
     * <div><b>Thread Safety:</b></div>
     * <p>
     * Instances of this class are mutable.
     * So use them carefully if they are accessed from multiple threads.
     * </p>
     *
     * @author Tomoki Sato
     * @see PushSubscription
     */
    public static class Keys {

        private String p256dh;
        private String auth;

        /**
         * Creates a new {@link Keys}.
         */
        public Keys() {
        }

        /**
         * Creates a new {@link Keys}.
         * All the fields are deeply copied from <code>another</code>.
         *
         * @param another an object from which the fields are copied.
         */
        public Keys(Keys another) {
            this.p256dh = another.getP256dh();
            this.auth = another.getAuth();
        }

        /**
         * Gets the p256dh.
         *
         * @return the value of the p256dh.
         */
        public String getP256dh() {
            return p256dh;
        }

        /**
         * Sets the p256dh.
         *
         * @param p256dh the value of the p256dh.
         */
        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        /**
         * Gets the auth.
         *
         * @return the value of the auth.
         */
        public String getAuth() {
            return auth;
        }

        /**
         * Sets the auth.
         *
         * @param auth the value of the auth.
         */
        public void setAuth(String auth) {
            this.auth = auth;
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
            if (!(o instanceof Keys)) {
                return false;
            }
            Keys keys = (Keys) o;
            return Objects.equals(getP256dh(), keys.getP256dh())
                && Objects.equals(getAuth(), keys.getAuth());
        }

        /**
         * Returns the hash code value for this object based on its properties.
         *
         * @return the hash code value for this object.
         */
        @Override
        public int hashCode() {
            return Objects.hash(getP256dh(), getAuth());
        }

        @Override
        public String toString() {
            return "Keys{"
                + "p256dh='" + p256dh + '\''
                + ", auth='" + auth + '\''
                + '}';
        }
    }

    /**
     * Gets the endpoint.
     *
     * @return the value of the endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint.
     *
     * @param endpoint the value of the endpoint.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the expiration time.
     *
     * @return the value of the expiration time.
     */
    public Long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the expiration time.
     *
     * @param expirationTime the value of the expiration time.
     */
    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Gets the keys.
     *
     * @return the value of the keys.
     */
    public Keys getKeys() {
        return keys;
    }

    /**
     * Sets the keys.
     *
     * @param keys the value of the keys.
     */
    public void setKeys(Keys keys) {
        this.keys = keys;
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
        if (!(o instanceof PushSubscription)) {
            return false;
        }
        PushSubscription that = (PushSubscription) o;
        return Objects.equals(getEndpoint(), that.getEndpoint())
            && Objects.equals(getExpirationTime(), that.getExpirationTime())
            && Objects.equals(getKeys(), that.getKeys());
    }

    /**
     * Returns the hash code value for this object based on its properties.
     *
     * @return the hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getEndpoint(), getExpirationTime(), getKeys());
    }

    @Override
    public String toString() {
        return "PushSubscription{"
            + "endpoint='" + endpoint + '\''
            + ", expirationTime='" + expirationTime + '\''
            + ", keys=" + keys
            + '}';
    }
}
