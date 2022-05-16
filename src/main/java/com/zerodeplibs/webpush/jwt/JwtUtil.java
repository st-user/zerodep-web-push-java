package com.zerodeplibs.webpush.jwt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;

/**
 * An internal utility class for JWT(Json Web Token) handling.
 */
class JwtUtil {

    private static final int R_LENGTH = 32;
    private static final int VR_LENGTH_INDEX = 3;
    private static final int S_LENGTH = 32;
    private static final int MAX_PADDING_LENGTH = 1;

    static String withSign(String header, String payload, ECPrivateKey privateKey) {

        byte[] headerBase64Bytes = encodeString(header);
        byte[] payloadBase64Bytes = encodeString(payload);

        byte[] message = new byte[headerBase64Bytes.length + payloadBase64Bytes.length + 1];

        System.arraycopy(headerBase64Bytes, 0, message, 0, headerBase64Bytes.length);
        message[headerBase64Bytes.length] = (byte) '.';
        System.arraycopy(payloadBase64Bytes, 0, message, headerBase64Bytes.length + 1,
            payloadBase64Bytes.length);

        try {
            byte[] signature = signWith(message, privateKey);
            return String.format("%s.%s.%s", asString(headerBase64Bytes),
                asString(payloadBase64Bytes), asString(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw VAPIDJWTCreationException.withDefaultMessage(e);
        }
    }

    private static String asString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private static byte[] signWith(byte[] data, ECPrivateKey privateKey)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(privateKey);
        sig.update(data);

        return encode(toJws(sig.sign()));
    }

    //Visible for testing
    static byte[] toJws(byte[] signature) {

        // DER: http://crypto.stackexchange.com/a/1797
        // JWS: https://datatracker.ietf.org/doc/html/rfc7515

        if (signature[0] != 0x30 || signature[2] != 0x02) {
            throw new VAPIDJWTCreationException("The format of the signature isn't valid DER");
        }

        int vrLength = signature[VR_LENGTH_INDEX];

        if (R_LENGTH + MAX_PADDING_LENGTH < vrLength
            ||
            (signature[VR_LENGTH_INDEX + vrLength + 1] != 0x02)) {

            throw new VAPIDJWTCreationException(
                String.format("The format of the signature isn't valid DER. (vrLength = %d)",
                    vrLength));
        }

        int vsLengthIndex = VR_LENGTH_INDEX + vrLength + 2;
        int vsLength = signature[vsLengthIndex];

        if (S_LENGTH + MAX_PADDING_LENGTH < vsLength) {
            throw new VAPIDJWTCreationException(
                String.format("The format of the signature isn't valid DER. (vsLength = %d)",
                    vsLength));
        }

        byte[] rs = new byte[R_LENGTH + S_LENGTH];
        int vrPadding = vrLength - R_LENGTH;
        int vsPadding = vsLength - S_LENGTH;

        System.arraycopy(
            signature,
            VR_LENGTH_INDEX + 1 + Math.max(0, vrPadding),
            rs,
            Math.max(0, -vrPadding),
            R_LENGTH - Math.max(0, -vrPadding));

        System.arraycopy(
            signature,
            vsLengthIndex + 1 + Math.max(0, vsPadding),
            rs,
            R_LENGTH + Math.max(0, -vsPadding),
            S_LENGTH - Math.max(0, -vsPadding));

        return rs;
    }

    private static byte[] encodeString(String data) {
        return encode(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] encode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encode(data);
    }
}
