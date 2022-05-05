package com.zerodeplibs.webpush.jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Test;

public class JwtUtilTests {


    @Test
    public void toJwsShouldConvertDerFormatToJwsFormat_ConsideringVrLengthProperly() {

        // vrLength = 33
        byte[] sigVrLength33 = toBytes(
            "MEYCIQDwja1T5JYgpE1kYCpensl0plyRSr7Xe4OZBpO0JAUwIwIhANpVro7r1tXntXmyDbhbABypLmVJ0+lW4Hip1V6LlKbr");
        assertThat(extractBytes(JwtUtil.toJws(sigVrLength33), 0, 32),
            equalTo(extractBytes(sigVrLength33, 5, 32)));

        // vrLength = 32
        byte[] sigVrLength32 = toBytes(
            "MEQCIApfEmThuzrwZkEWIzAl70VVyVPxuVtl2e7Nf523VubHAiBrc7eLri2lnc5smwqDDL1Nz61X4dDA840zjM6dSsQ0+w==");
        assertThat(extractBytes(JwtUtil.toJws(sigVrLength32), 0, 32),
            equalTo(extractBytes(sigVrLength32, 4, 32)));

        // vrLength = 31
        byte[] sigVrLength31 = toBytes(
            "MEQCH1Hywl5ZYhedPcUtITBP08iD0fAQkAiXIywznt548EgCIQDq3S5GbAQCij5ub61N+1cbfKaEMHRgB/GBUMaM/a9OIA==");
        byte[] jwsFor31 = JwtUtil.toJws(sigVrLength31);
        assertThat(jwsFor31[0], equalTo((byte) 0));
        assertThat(extractBytes(jwsFor31, 1, 31),
            equalTo(extractBytes(sigVrLength31, 4, 31)));
    }

    @Test
    public void toJwsShouldConvertDerFormatToJwsFormat_ConsideringVsLengthProperly() {

        // vsLength = 33
        byte[] sigVsLength33 = toBytes(
            "MEUCICT9wNy8iXHsrXhptDbJauOkL0rG6WKWhmnYWD5m5KHwAiEAzQmJ57+SZYX3p0fcAnJSu+QqtN9os5kNSYTzmPAmsX0=");
        assertThat(extractBytes(JwtUtil.toJws(sigVsLength33), 32, 32),
            equalTo(extractBytes(sigVsLength33, sigVsLength33.length - 32, 32)));

        byte[] sigVsLength32 = toBytes(
            "MEUCIQCawzUA84L2AhLsVwzlHttZpJHxdGxhWQarXCfdsEKhcgIgO8e8AAopyDkAzuQdRznzXp/S15zwD/lv060HkzVW9JQ=");
        assertThat(extractBytes(JwtUtil.toJws(sigVsLength32), 32, 32),
            equalTo(extractBytes(sigVsLength32, sigVsLength32.length - 32, 32)));

        byte[] sigVsLength31 = toBytes(
            "MEQCIQChrgqn6sGU8KBhuFe0YzAfdNY07HXE37+4Lqk1idfEfAIfU8gp1Wsd4o0cVyq128MKIFefevi07kMr/utXrca68g==");
        byte[] jwsFor31 = JwtUtil.toJws(sigVsLength31);
        assertThat(jwsFor31[32], equalTo((byte) 0));
        assertThat(extractBytes(JwtUtil.toJws(sigVsLength31), 33, 31),
            equalTo(extractBytes(sigVsLength31, sigVsLength31.length - 31, 31)));
    }

    private byte[] extractBytes(byte[] data, int start, int length) {
        return Arrays.copyOfRange(data, start, start + length);
    }

    private byte[] toBytes(String data) {
        return Base64.getDecoder().decode(data);
    }
}
