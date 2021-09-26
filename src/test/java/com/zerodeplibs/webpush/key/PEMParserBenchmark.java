package com.zerodeplibs.webpush.key;

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

public class PEMParserBenchmark {

    public static void main(String[] args) throws RunnerException {

        Options options = new OptionsBuilder()
            .include(PEMParserBenchmark.class.getSimpleName())
            .build();

        new Runner(options).run();
    }

    @State(Scope.Benchmark)
    public static class PlanForPrivateKey {

        String text;

        @Setup(Level.Trial)
        public void setUp() {
            this.text = "-----BEGIN PRIVATE KEY-----\r\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgry+xfRuVtpCPOmxc\r\n" +
                "aexkSXhua7zpaclMit3Un3ak+dqhRANCAARUzN0lXeB4j2/nljd8PovHUz/tC4X0\r\n" +
                "Sv4MSSN/2Tbx9ElHccgGa4uhw5ueoORaTRQ96SYmBFE4xJa3xBiMtfBR\r\n" +
                "-----END PRIVATE KEY-----";
        }
    }

    @State(Scope.Benchmark)
    public static class PlanForPublicKey {

        String text;

        @Setup(Level.Trial)
        public void setUp() {
            this.text = "-----BEGIN PUBLIC KEY-----\r\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEVMzdJV3geI9v55Y3fD6Lx1M/7QuF\r\n" +
                "9Er+DEkjf9k28fRJR3HIBmuLocObnqDkWk0UPekmJgRROMSWt8QYjLXwUQ==\r\n" +
                "-----END PUBLIC KEY-----\r\n";
        }
    }

    @Benchmark
    @Fork(value = 2)
    @Warmup(iterations = 2)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Measurement(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void privateKey(Blackhole h, PlanForPrivateKey plan) {
        h.consume(PEMParsers.ofStandard("PRIVATE KEY").parse(plan.text));
    }

    @Benchmark
    @Fork(value = 2)
    @Warmup(iterations = 2)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Measurement(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void publicKey(Blackhole h, PlanForPublicKey plan) {
        h.consume(PEMParsers.ofStandard("PUBLIC KEY").parse(plan.text));
    }
}
