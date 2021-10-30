package com.zerodeplibs.webpush.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zerodeplibs.webpush.MessageEncryptionTestUtil;
import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;

class PreparerTestUtil {


    static PushSubscription createPushSubscription(String endpoint)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPair keyPair = MessageEncryptionTestUtil.generateKeyPair();
        String p256dh =
            MessageEncryptionTestUtil.generateP256dhString((ECPublicKey) keyPair.getPublic());
        String auth = MessageEncryptionTestUtil.generateAuthSecretString();

        PushSubscription pushSubscription = new PushSubscription();
        PushSubscription.Keys keys = new PushSubscription.Keys();
        keys.setP256dh(p256dh);
        keys.setAuth(auth);
        pushSubscription.setEndpoint(endpoint);
        pushSubscription.setKeys(keys);

        return pushSubscription;
    }

    static class TestingVAPIDKeyPair implements VAPIDKeyPair {

        private final VAPIDJWTParam expectedParameters;

        TestingVAPIDKeyPair(VAPIDJWTParam expectedParameters) {
            this.expectedParameters = expectedParameters;
        }

        @Override
        public byte[] extractPublicKeyInUncompressedForm() {
            throw new UnsupportedOperationException("Not supported on testing.");
        }

        @Override
        public String generateAuthorizationHeaderValue(VAPIDJWTParam jwtParam) {
            if (expectedParameters != null) {
                assertThat(jwtParam, equalTo(expectedParameters));
            }
            return "vapid for test";
        }
    }
}
