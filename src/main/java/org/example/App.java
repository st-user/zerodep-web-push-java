package org.example;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.zerodeplibs.webpush.EncryptedPushMessage;
import com.zerodeplibs.webpush.MessageEncryption;
import com.zerodeplibs.webpush.MessageEncryptions;
import com.zerodeplibs.webpush.PushMessage;
import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.UserAgentMessageEncryptionKeyInfo;
import com.zerodeplibs.webpush.VAPIDKeyPair;
import com.zerodeplibs.webpush.VAPIDKeyPairs;
import com.zerodeplibs.webpush.header.TTL;
import com.zerodeplibs.webpush.header.Topic;
import com.zerodeplibs.webpush.header.Urgency;
import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import com.zerodeplibs.webpush.key.PrivateKeySources;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.io.File;
import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {

    // Implement VAPIDJWTGenerator with an arbitrary JWT library.
    static class MyAuth0VAPIDJWTGenerator implements VAPIDJWTGenerator {

        private final Algorithm jwtAlgorithm;

        MyAuth0VAPIDJWTGenerator(ECPrivateKey privateKey, ECPublicKey publicKey) {
            this.jwtAlgorithm = Algorithm.ECDSA256(publicKey, privateKey);
        }

        @Override
        public String generate(VAPIDJWTParam vapidjwtParam) {
            return JWT.create()
                .withAudience(vapidjwtParam.getOrigin())
                .withExpiresAt(vapidjwtParam.getExpiresAt())
                .withSubject(vapidjwtParam.getSubject().orElse("mailto:example@example.com"))
                .sign(this.jwtAlgorithm);
        }
    }

    @Autowired
    private VAPIDKeyPair vapidKeyPair;

    // In this example, reads the key pair for VAPID from the file system.
    @Bean
    public VAPIDKeyPair vaidKeyPair(
        @Value("${private.key.file.path}") String privateKeyFilePath,
        @Value("${public.key.file.path}") String publicKeyFilePath) throws IOException {

        return VAPIDKeyPairs.of(
            PrivateKeySources.ofPEMFile(new File(privateKeyFilePath).toPath()),
            PublicKeySources.ofPEMFile(new File(publicKeyFilePath).toPath()),
            MyAuth0VAPIDJWTGenerator::new
            // (privateKey, publicKey) -> new MyJose4jVAPIDJWTGenerator(privateKey)
            // (privateKey, publicKey) -> new MyNimbusJoseJwtVAPIDJWTGenerator(privateKey)
        );
    }

    /**
     * # Step 1.
     * Sends the public key to user agents.
     *
     * The user agents create push subscriptions with this public key.
     */
    @GetMapping("/getPublicKey")
    public byte[] getPublicKey() {
        return vapidKeyPair.extractPublicKeyInUncompressedForm();
    }

    /**
     * # Step 2.
     * Obtains push subscriptions from user agents.
     *
     * The application server(this application) requests the delivery of push messages with these subscriptions.
     */
    @PostMapping("/subscribe")
    public void subscribe(@RequestBody PushSubscription subscription) {
        this.saveSubscriptionToStorage(subscription);
    }

    /**
     * # Step 3.
     * Requests the delivery of push messages.
     *
     * In this example, for simplicity and testability, we implement this feature as an HTTP endpoint.
     * However, in real applications, this feature does not have to be an HTTP endpoint.
     */
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> messages)
        throws IOException {

        String message = messages.getOrDefault("message", "").trim();
        message = message.length() == 0 ? "Default Message." : message;

        OkHttpClient httpClient = new OkHttpClient();
        MessageEncryption messageEncryption = MessageEncryptions.of();

        for (PushSubscription subscription : getSubscriptionsFromStorage()) {

            VAPIDJWTParam vapidjwtParam = VAPIDJWTParam.getBuilder()
                .resourceURLString(subscription.getEndpoint())
                .expiresAfterSeconds((int) TimeUnit.MINUTES.toSeconds(15))
                .subject("mailto:example@example.com")
                .build();

            EncryptedPushMessage encryptedPushMessage = messageEncryption.encrypt(
                UserAgentMessageEncryptionKeyInfo.from(subscription.getKeys()),
                PushMessage.ofUTF8(message)
            );

            Request request = new Request.Builder()
                .url(subscription.getEndpoint())
                .addHeader("Authorization",
                    vapidKeyPair.generateAuthorizationHeaderValue(vapidjwtParam))
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Content-Encoding", "aes128gcm")
                .addHeader("TTL", String.valueOf(TTL.hours(1)))
                .addHeader("Urgency", Urgency.low())
                .addHeader("Topic", Topic.ensure("myTopic"))
                .post(okhttp3.RequestBody.create(encryptedPushMessage.toBytes()))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                logger.info(String.format("status code: %d", response.code()));
            }

        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body("The message has been processed.");
    }

    private Collection<PushSubscription> getSubscriptionsFromStorage() {
        return this.subscriptionMap.values();
    }

    private void saveSubscriptionToStorage(PushSubscription subscription) {
        this.subscriptionMap.put(subscription.getEndpoint(), subscription);
    }

    private final Logger logger = LoggerFactory.getLogger(App.class);
    private final Map<String, PushSubscription> subscriptionMap = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
