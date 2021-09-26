package com.zerodeplibs.webpush;

import com.zerodeplibs.webpush.key.PublicKeySources;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation class of {@link MessageEncryption}.
 * This class uses the <a href="https://datatracker.ietf.org/doc/html/rfc8188">aes128Gcm</a> algorithm for encryption.
 *
 * @author Tomoki Sato
 */
class Aes128GcmMessageEncryption implements MessageEncryption {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Mac mac;
    private final byte[] keyInfoPref = toInfoBytes("WebPush: info");
    private final byte[] cekInfo = toInfoBytes("Content-Encoding: aes128gcm");
    private final byte[] nonceInfo = toInfoBytes("Content-Encoding: nonce");

    Aes128GcmMessageEncryption() throws NoSuchAlgorithmException {
        this.mac = Mac.getInstance("HmacSHA256");
    }

    private static byte[] toInfoBytes(String text) {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] ret = new byte[textBytes.length + 1];
        System.arraycopy(textBytes, 0, ret, 0, textBytes.length);
        ret[ret.length - 1] = 0;
        return ret;
    }

    @Override
    public EncryptedPushMessage encrypt(
        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys, PushMessage pushMessage) {

        try {
            return encryptInternal(userAgentMessageEncryptionKeys, pushMessage);
        } catch (InvalidAlgorithmParameterException
            | NoSuchAlgorithmException
            | InvalidKeyException
            | IllegalBlockSizeException
            | NoSuchPaddingException
            | BadPaddingException e) {

            throw new WebPushRuntimeWrapperException(e);
        }
    }

    byte[] decrypt(
        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys,
        Aes128GcmEncryptedMessage encrypted,
        ECPrivateKey uaPrivate) {

        try {
            return decryptInternal(userAgentMessageEncryptionKeys, encrypted, uaPrivate);
        } catch (NoSuchAlgorithmException
            | InvalidKeyException
            | InvalidAlgorithmParameterException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            throw new WebPushRuntimeWrapperException(e);
        }
    }

    private EncryptedPushMessage encryptInternal(
        UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys, PushMessage payload)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException,
        IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {

        ECPublicKey uaPublic = userAgentMessageEncryptionKeys.getPublicKey();

        KeyPair asKeyPair = generateAsKeyPair();
        PrivateKey asPrivate = asKeyPair.getPrivate();
        PublicKey asPublic = asKeyPair.getPublic();
        byte[] asPublicUncompressed =
            PublicKeySources.ofECPublicKey((ECPublicKey) asPublic).extractUncompressedBytes();

        byte[] ecdhSecret = calcECDHSecret(asPrivate, uaPublic);
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        CekAndNonce cekAndNonce = calcCekAndNonce(
            salt,
            ecdhSecret,
            userAgentMessageEncryptionKeys.getAuthSecret(),
            userAgentMessageEncryptionKeys.getUncompressedUaPublic(),
            asPublicUncompressed
        );
        byte[] cek = cekAndNonce.getCek();
        byte[] nonce = cekAndNonce.getNone();

        byte[] encrypted =
            encryptByAesGcm(nonce, cek,
                concatByteArrays(payload.getMessageBytes(), new byte[] {2}));

        byte[] header = concatByteArrays(
            salt,
            toRecordSizeBytes(encrypted.length),
            new byte[] {(byte) asPublicUncompressed.length},
            asPublicUncompressed
        );

        byte[] encryptedBytes = concatByteArrays(header, encrypted);

        return new Aes128GcmEncryptedMessage(encryptedBytes);
    }

    private byte[] decryptInternal(UserAgentMessageEncryptionKeys userAgentMessageEncryptionKeys,
                                   Aes128GcmEncryptedMessage encrypted,
                                   ECPrivateKey uaPrivate)
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException,
        NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        byte[] salt = encrypted.extractSalt();

        byte[] uncompressedAsPublicKeyBytes = encrypted.extractUncompressedAsPublicKeyBytes();
        byte[] content = encrypted.extractContent();

        ECPublicKey asPublic =
            PublicKeySources.ofUncompressedBytes(uncompressedAsPublicKeyBytes).extract();

        byte[] ecdhSecret = calcECDHSecret(uaPrivate, asPublic);

        CekAndNonce cekAndNonce = calcCekAndNonce(
            salt,
            ecdhSecret,
            userAgentMessageEncryptionKeys.getAuthSecret(),
            userAgentMessageEncryptionKeys.getUncompressedUaPublic(),
            uncompressedAsPublicKeyBytes
        );

        byte[] cek = cekAndNonce.getCek();
        byte[] nonce = cekAndNonce.getNone();

        byte[] decrypted = decryptByAesGcm(nonce, cek, content);

        return stripPadding(decrypted);
    }


    private KeyPair generateAsKeyPair()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));

        return keyPairGenerator.genKeyPair();
    }

    private byte[] calcECDHSecret(PrivateKey asPrivate, PublicKey uaPublic)
        throws NoSuchAlgorithmException, InvalidKeyException {

        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(asPrivate);
        keyAgreement.doPhase(uaPublic, true);

        return keyAgreement.generateSecret();
    }

    private CekAndNonce calcCekAndNonce(
        byte[] salt,
        byte[] ecdhSecret,
        byte[] authSecret,
        byte[] uaPublicUncompressed,
        byte[] asPublicUncompressed
    ) throws InvalidKeyException {
        // ## Use HKDF to combine the ECDH and authentication secrets

        // # HKDF-Extract(salt=auth_secret, IKM=ecdh_secret)
        // PRK_key = HMAC-SHA-256(auth_secret, ecdh_secret)
        byte[] prkKey = hmac(authSecret, ecdhSecret);

        // # HKDF-Expand(PRK_key, key_info, L_key=32)
        // key_info = "WebPush: info" || 0x00 || ua_public || as_public
        byte[] keyInfo = concatByteArrays(
            keyInfoPref,
            uaPublicUncompressed,
            asPublicUncompressed
        );
        byte[] ikm = hmac(prkKey, concatByteArrays(keyInfo, new byte[] {0x01}));

        // ## HKDF calculations from RFC 8188
        // # HKDF-Extract(salt, IKM)
        // PRK = HMAC-SHA-256(salt, IKM)
        byte[] prk = hmac(salt, ikm);

        // # HKDF-Expand(PRK, cek_info, L_cek=16)
        // cek_info = "Content-Encoding: aes128gcm" || 0x00
        // CEK = HMAC-SHA-256(PRK, cek_info || 0x01)[0..15]
        byte[] cek = hmac(prk, concatByteArrays(cekInfo, new byte[] {0x01}));
        cek = Arrays.copyOfRange(cek, 0, 16);

        // # HKDF-Expand(PRK, nonce_info, L_nonce=12)
        // nonce_info = "Content-Encoding: nonce" || 0x00
        // NONCE = HMAC-SHA-256(PRK, nonce_info || 0x01)[0..11]
        byte[] nonce = hmac(prk, concatByteArrays(nonceInfo, new byte[] {0x01}));
        nonce = Arrays.copyOfRange(nonce, 0, 12);

        return new CekAndNonce(cek, nonce);
    }

    private byte[] hmac(byte[] salt, byte[] message) throws InvalidKeyException {

        SecretKeySpec secretKeySpec =
            new SecretKeySpec(salt, "HmacSHA256");

        mac.init(secretKeySpec);
        return mac.doFinal(message);
    }

    private byte[] encryptByAesGcm(byte[] iv, byte[] secretKey, byte[] data)
        throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException,
        BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        return processByteAesGcm(iv, secretKey, data, Cipher.ENCRYPT_MODE);
    }

    private byte[] decryptByAesGcm(byte[] iv, byte[] secretKey, byte[] data)
        throws InvalidAlgorithmParameterException, NoSuchPaddingException,
        IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException,
        InvalidKeyException {
        return processByteAesGcm(iv, secretKey, data, Cipher.DECRYPT_MODE);
    }

    private byte[] stripPadding(byte[] decryptedBytes) {

        int endIndexExclusive = -1;
        for (int i = decryptedBytes.length - 1; 0 <= i; i--) {
            byte v = decryptedBytes[i];

            if (v == 0) {
                continue;
            }

            if (v != 2) {
                throw new IllegalArgumentException(
                    "A single aes128gcm record must contain a padding delimiter whose value is 2.");
            }

            // v == 2
            endIndexExclusive = i;
            break;
        }

        if (endIndexExclusive == -1) {
            throw new IllegalArgumentException("The record doesn't contain no non-zero octet.");
        }

        return Arrays.copyOfRange(decryptedBytes, 0, endIndexExclusive);
    }


    private byte[] processByteAesGcm(byte[] iv, byte[] secretKey, byte[] data, int encryptMode)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
        InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec param = new GCMParameterSpec(128, iv);

        cipher.init(encryptMode, secretKeySpec, param);

        return cipher.doFinal(data);
    }

    private byte[] toRecordSizeBytes(int size) {
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.putInt(size);
        return bf.array();
    }

    private byte[] concatByteArrays(byte[]... byteArrays) {
        int totalLen = Arrays.stream(byteArrays)
            .mapToInt(arr -> arr.length)
            .sum();

        byte[] ret = new byte[totalLen];
        int currentDestStart = 0;
        for (byte[] arr : byteArrays) {
            System.arraycopy(arr, 0, ret, currentDestStart, arr.length);
            currentDestStart += arr.length;
        }
        return ret;
    }

    private static class CekAndNonce {

        private final byte[] cek;
        private final byte[] none;

        CekAndNonce(byte[] cek, byte[] none) {
            this.cek = cek;
            this.none = none;
        }

        byte[] getCek() {
            return cek;
        }

        byte[] getNone() {
            return none;
        }
    }
}
