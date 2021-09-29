package com.zerodeplibs.webpush;

import java.util.Objects;

/**
 * This class represents a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a> described in the <a href="https://www.w3.org/TR/push-api/">Push API - W3C</a> specification.
 *
 * <p>
 * The values for the fields in this class are intended to be set
 * from the push subscription JSON objects retrieved from browsers.
 * </p>
 *
 * <p>
 * The following is an implementation example of javascript on the browser side
 * to get the JSON object of push subscription.
 *
 * </p>
 * <pre class="code">
 *
 *     // It is assumed that you have registered a service worker
 *     const registration = await navigator.serviceWorker.ready;
 *     const subscription = await registration.pushManager.subscribe({
 *         userVisibleOnly: true,
 *         applicationServerKey: serverPublicKey // this your server's public key.
 *     });
 *
 *     // Send the subscription to your server.
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
 * Since setters corresponding to the field names of push subscription are defined in this class,
 * it can be deserialized using a library that handles JSON such as <a href="https://github.com/FasterXML/jackson">jackson</a>.
 * </p>
 *
 * @author Tomoki Sato
 * @see UserAgentMessageEncryptionKeyInfo
 */
public class PushSubscription {

    private String endpoint;
    private String expirationTime;
    private Keys keys;

    /**
     * This class represents a '<a href="https://www.w3.org/TR/push-api/#pushsubscription-interface">keys</a>' field for a <a href="https://www.w3.org/TR/push-api/#push-subscription">push subscription</a>.
     *
     * @author Tomoki Sato
     */
    public static class Keys {

        private String p256dh;
        private String auth;

        public String getP256dh() {
            return p256dh;
        }

        public void setP256dh(String p256dh) {
            this.p256dh = p256dh;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }


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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Keys getKeys() {
        return keys;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

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
