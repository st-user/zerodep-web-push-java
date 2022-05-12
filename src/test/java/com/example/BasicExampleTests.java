package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodeplibs.webpush.PushSubscription;
import com.zerodeplibs.webpush.key.PublicKeySources;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Objects;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;


@ExtendWith(SpringExtension.class)
@WebFluxTest(BasicExample.class)
@Import(MyComponents.class)
@AutoConfigureWebTestClient(timeout = "PT5M") //5 minutes
public class BasicExampleTests {

    @Autowired
    private WebTestClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer server;

    @AfterEach
    void shutdown() throws IOException {
        if (server != null) {
            this.server.shutdown();
        }
    }

    void startServer() throws IOException {
        this.server = new MockWebServer();
        this.server.start(9876);
        this.server.url("/");
        this.server.setDispatcher(new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

                switch (Objects.requireNonNull(request.getPath())) {
                    case "/a":
                        return new MockResponse().setResponseCode(201);
                    case "/b":
                        return new MockResponse().setResponseCode(429);
                    case "/c":
                        return new MockResponse().setResponseCode(500);
                    case "/d":
                        return new MockResponse().setResponseCode(401);
                    case "/e":
                        return new MockResponse().setResponseCode(302);
                    case "/f":
                        return new MockResponse().setResponseCode(200);
                    case "/g":
                        return new MockResponse().setResponseCode(410);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
    }

    @Test
    void sendRequestsForDeliveryOfPushNotification() throws Exception {
        startServer();

        postSubscription("http://localhost:9876/a"); // 201
        postSubscription("http://localhost:9876/b"); // 429 Too Many Request
        postSubscription("http://localhost:9876/c"); // 500
        postSubscription(
            createInvalidPushSubscription("http://invalid.example.com/x")); // Invalid Subscription
        postSubscription("http://localhost:9876/d"); // 401 Unauthorized
        postSubscription("http://localhost:9876/e"); // 302 Found
        postSubscription("http://localhost:9876/f"); // 200 Ok
        postSubscription("http://localhost:9876/g"); // 410 Gone

        BasicExample.RequestResultResponse responseBody = webClient.post()
            .uri("/sendMessage")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"message\":\"Test\"}")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(BasicExample.RequestResultResponse.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertEquals(2, responseBody.getTotalSuccess());
        Assertions.assertEquals(6, responseBody.getTotalFailure());
    }

    private void postSubscription(String endpoint)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        postSubscription(createPushSubscription(endpoint));
    }

    private void postSubscription(PushSubscription subscription) {
        webClient
            .post()
            .uri("/subscribe")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(subscription)
            .exchange()
            .expectStatus()
            .isOk();
    }

    private PushSubscription createPushSubscription(String endpoint)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPair keyPair = generateKeyPair();
        String p256dh = generateP256dhString((ECPublicKey) keyPair.getPublic());
        String auth = generateAuthSecretString();

        PushSubscription pushSubscription = new PushSubscription();
        PushSubscription.Keys keys = new PushSubscription.Keys();
        keys.setP256dh(p256dh);
        keys.setAuth(auth);
        pushSubscription.setEndpoint(endpoint);
        pushSubscription.setKeys(keys);

        return pushSubscription;
    }

    private PushSubscription createInvalidPushSubscription(String endpoint) {
        PushSubscription pushSubscription = new PushSubscription();
        pushSubscription.setEndpoint(endpoint);
        return pushSubscription;
    }

    private KeyPair generateKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

        return keyPairGenerator.genKeyPair();
    }

    private String generateP256dhString(ECPublicKey uaPublic) {
        byte[] uncompressedBytes =
            PublicKeySources.ofECPublicKey(uaPublic).extractBytesInUncompressedForm();
        return toBase64Url(uncompressedBytes);
    }

    private String generateAuthSecretString() {
        byte[] authSecret = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(authSecret);
        return toBase64Url(authSecret);
    }

    private static String toBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
