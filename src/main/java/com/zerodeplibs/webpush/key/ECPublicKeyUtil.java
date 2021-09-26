package com.zerodeplibs.webpush.key;

import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.util.Base64;

/**
 * The internal utility class for handling ECDSA public keys(mainly secp256r1).
 *
 * @author Tomoki Sato
 */
class ECPublicKeyUtil {

    private static final byte[] P256_HEAD =
        Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");


    static byte[] uncompressedBytesToX509Bytes(byte[] uncompressedBytes) {
        byte[] encoded = new byte[P256_HEAD.length + uncompressedBytes.length];
        System.arraycopy(P256_HEAD, 0, encoded, 0, P256_HEAD.length);
        System.arraycopy(uncompressedBytes, 0, encoded, P256_HEAD.length, uncompressedBytes.length);
        return encoded;
    }

    static byte[] encodedBytesToUncompressedBytes(byte[] encoded) {
        byte[] ret = new byte[65];
        int elemCount = encoded.length - P256_HEAD.length;
        System.arraycopy(encoded, P256_HEAD.length, ret, 0, elemCount);
        return ret;
    }

    static void validateECPublicKey(ECPublicKey publicKey) {

        ECPoint w = publicKey.getW();
        ECParameterSpec params = publicKey.getParams();

        if (params.getCofactor() != 1) {
            throw new InvalidECPublicKeyException(
                "This method can't provide sufficient validation if h != 1.");
        }

        if (ECPoint.POINT_INFINITY.equals(w)) {
            throw new InvalidECPublicKeyException("Point at infinity.");
        }

        BigInteger x = publicKey.getW().getAffineX();
        BigInteger y = publicKey.getW().getAffineY();

        ECField field = publicKey.getParams().getCurve().getField();
        BigInteger p = ((ECFieldFp) field).getP();

        if (x.compareTo(BigInteger.ZERO) < 0 || x.compareTo(p) >= 0
            || y.compareTo(BigInteger.ZERO) < 0 || y.compareTo(p) >= 0) {
            throw new InvalidECPublicKeyException("Both x and y are not in range [0, p-1].");
        }

        BigInteger a = params.getCurve().getA();
        BigInteger b = params.getCurve().getB();

        BigInteger l = y.modPow(BigInteger.valueOf(2), p);
        BigInteger r = x.pow(3)
            .add(a.multiply(x))
            .add(b)
            .mod(p);

        if (!l.equals(r)) {
            throw new InvalidECPublicKeyException("y^2 != x^3 + ax + b (mod p)");
        }

    }
}
