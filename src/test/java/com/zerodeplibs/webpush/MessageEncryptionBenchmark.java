package com.zerodeplibs.webpush;

import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateAuthSecretString;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateKeyPair;
import static com.zerodeplibs.webpush.MessageEncryptionTestUtil.generateP256dhString;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MessageEncryptionBenchmark {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
            .include(MessageEncryptionBenchmark.class.getSimpleName())
            .build();

        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class PlanForEncryptionOnly {

        UserAgentMessageEncryptionKeys uaKeys;
        PushMessage pushMessage;
        MessageEncryption messageEncryption;

        @Setup(Level.Trial)
        public void setUp() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

            KeyPair uaKeyPair = generateKeyPair();
            ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
            String p256dh = generateP256dhString(uaPublic);
            String auth = generateAuthSecretString();
            String payload = "Hello World. This is a payload for testing.";

            this.uaKeys = UserAgentMessageEncryptionKeys.of(p256dh, auth);
            this.pushMessage = PushMessage.ofUTF8(payload);
            this.messageEncryption = MessageEncryptions.of();
        }
    }

    @State(Scope.Benchmark)
    public static class PlanForEncryptionProcess {

        String p256dh;
        String auth;
        String payload;

        @Setup(Level.Trial)
        public void setUp() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

            KeyPair uaKeyPair = generateKeyPair();
            ECPublicKey uaPublic = (ECPublicKey) uaKeyPair.getPublic();
            this.p256dh = generateP256dhString(uaPublic);
            this.auth = generateAuthSecretString();
            this.payload = "Hello World. This is a payload for testing.";

        }
    }

    @Benchmark
    @Fork(value = 2)
    @Warmup(iterations = 2)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void onlyEncryption(Blackhole h, PlanForEncryptionOnly plan) {
        h.consume(plan.messageEncryption.encrypt(
            plan.uaKeys, plan.pushMessage
        ));
    }

    @Benchmark
    @Fork(value = 2)
    @Warmup(iterations = 2)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Measurement(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void encryptionProcess(Blackhole h, PlanForEncryptionProcess plan) {

        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys =
            UserAgentMessageEncryptionKeys.of(plan.p256dh, plan.auth);
        MessageEncryption messageEncryption = MessageEncryptions.of();

        EncryptedPushMessage encrypted = messageEncryption.encrypt(
            userAgentMessageEncryptionKeys,
            PushMessage.ofUTF8(plan.payload)
        );

        h.consume(encrypted);
    }
}
