package org.example;

import com.zerodeplibs.webpush.jwt.VAPIDJWTGenerator;
import com.zerodeplibs.webpush.jwt.VAPIDJWTParam;
import java.security.interfaces.ECPrivateKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.slf4j.LoggerFactory;

/**
 * The example implementation of {@link VAPIDJWTGenerator}
 * utilizing <a href="https://bitbucket.org/b_c/jose4j/wiki/Home">jose4j</a>.
 *
 *
 * <p>
 * <b>Notice about JSON processing:</b>
 * When using jose4j, it is recommended to use a JSON library
 * such as Jackson, Gson, JSON-B to create JWT payloads
 * (to illustrate an implementation that uses only jose4j,
 * this class generates payloads without using such libraries).
 * In this class, for simplicity:
 * </p>
 * <ul>
 * <li>the functionality for escaping characters is omitted.</li>
 * <li>the functionality for mapping objects to JSONs isn't fully implemented.</li>
 * </ul>
 */
public class MyJose4jVAPIDJWTGenerator implements VAPIDJWTGenerator {

    private final ECPrivateKey privateKey;

    public MyJose4jVAPIDJWTGenerator(ECPrivateKey privateKey) {
        LoggerFactory.getLogger(getClass()).info("Using " + getClass().getSimpleName());
        this.privateKey = privateKey;
    }

    @Override
    public String generate(VAPIDJWTParam param) {

        JsonWebSignature jws = new JsonWebSignature();

        jws.setHeader("typ", "JWT");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);

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

        jws.setPayload(payloadBuilder.toString());
        jws.setKey(privateKey);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    private void appendStringJsonProp(String name, String value, StringBuilder builder) {
        appendStringJsonToken(name, builder);
        builder.append(':');
        appendStringJsonToken(value, builder);
    }

    @Deprecated
    private void appendJsonProp(String name, String value, StringBuilder builder) {
        appendStringJsonToken(name, builder);
        builder.append(':');
        builder.append(value);
    }

    @Deprecated
    private void appendStringJsonToken(String value, StringBuilder builder) {
        builder.append("\"");
        builder.append(value);
        builder.append("\"");
    }

    @Deprecated
    private String convValue(Object o) {
        // FIXME Implement conversions according to your requirements.
        if (o == null) {
            return "null";
        }
        if (o instanceof String) {
            return "\"" + (String) o + "\"";
        }
        return o.toString();
    }
}
