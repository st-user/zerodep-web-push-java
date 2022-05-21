package com.zerodeplibs.webpush.jwt;

import java.math.BigDecimal;
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.Date;

/**
 * An implementation of {@link VAPIDJWTGenerator}.
 *
 * @author Tomoki Sato
 * @see com.zerodeplibs.webpush.VAPIDKeyPairs
 * @see DefaultVAPIDJWTGeneratorFactory
 */
class DefaultVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    DefaultVAPIDJWTGenerator(ECPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Generates a JWT used for VAPID.
     *
     * <p>
     * If the given param has additional claims, these values
     * must be an instance of {@link String}, {@link Boolean},
     * {@link Integer}, {@link Long}, {@link Double}, {@link Date} or {@link Instant}.
     * </p>
     *
     * @param param parameters used to build the JWT.
     * @return a JWT.
     * @throws IllegalArgumentException if the value of an additional claim
     *                                  is not an instance of a supported type.
     */
    @Override
    public String generate(VAPIDJWTParam param) {


        /* Starts building the payload. */
        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append('{');

        appendStringJsonProp("aud", param.getOrigin(), payloadBuilder);

        payloadBuilder.append(',');
        appendJsonProp("exp", String.valueOf(param.getExpiresAtInSeconds()), payloadBuilder);

        param.getSubject().ifPresent(sub -> {
            payloadBuilder.append(',');
            appendStringJsonProp("sub", sub, payloadBuilder);
        });

        param.forEachAdditionalClaim((name, claim) -> {
            payloadBuilder.append(',');
            appendJsonProp(name, convValue(claim), payloadBuilder);
        });

        payloadBuilder.append('}');
        /* Ends building the payload. */

        String header = "{\"typ\":\"JWT\",\"alg\":\"ES256\"}";
        String payload = payloadBuilder.toString();

        return JwtUtil.withSign(header, payload, this.privateKey);
    }

    private void appendStringJsonProp(String name, String value, StringBuilder builder) {
        appendStringJsonToken(name, builder);
        builder.append(':');
        appendStringJsonToken(value, builder);
    }

    private void appendJsonProp(String name, String value, StringBuilder builder) {
        appendStringJsonToken(name, builder);
        builder.append(':');
        builder.append(value);
    }

    private void appendStringJsonToken(String value, StringBuilder builder) {
        builder.append("\"");
        builder.append(value);
        builder.append("\"");
    }

    private String convValue(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof String) {
            return "\"" + o + "\"";
        }
        if (o instanceof Boolean || o instanceof Integer || o instanceof Long) {
            return o.toString();
        }
        if (o instanceof Double) {
            return BigDecimal.valueOf((Double) o).stripTrailingZeros().toPlainString();
        }
        if (o instanceof Date) {
            Date d = (Date) o;
            return String.valueOf(d.getTime() / 1000);
        }
        if (o instanceof Instant) {
            Instant instant = (Instant) o;
            return String.valueOf(instant.toEpochMilli() / 1000);
        }

        throw new IllegalArgumentException(
            "The value of an additional claim must be an instance of "
                + "String, Boolean, Integer, Long, Double, Date or Instant.");
    }
}
