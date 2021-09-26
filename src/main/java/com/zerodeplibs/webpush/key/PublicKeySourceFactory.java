package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPublicKey;

/**
 * Implementations of this interface provide the functionality
 * for creating instances of {@link PublicKeySource}.
 *
 * <p>
 * Typically, to create a PublicKeySource,
 * a factory method defined in {@link PublicKeySources} is used.
 * </p>
 *
 * @author Tomoki Sato
 */
public interface PublicKeySourceFactory {

    /**
     * Create a PublicKeySource with the byte array representing
     * a public key on the P-256 curve encoded in the uncompressed form[X9.62].
     *
     * @param uncompressedBytes the byte array representing a public key.
     * @return a new PublicKeySource.
     * @throws MalformedUncompressedBytesException if the given array doesn't start with 0x4
     *                                             or the length isn't 65 bytes.
     */
    PublicKeySource fromUncompressedBytes(byte[] uncompressedBytes);

    /**
     * Creates a PublicKeySource with the byte array
     * that is assumed to be encoded according to the X.509 standard.
     *
     * @param x509Bytes the byte array representing a public key.
     * @return a new PublicKeySource.
     * @see java.security.spec.X509EncodedKeySpec
     */
    PublicKeySource fromX509Bytes(byte[] x509Bytes);

    /**
     * Creates a PublicKeySource that wraps the given ECPublicKey object.
     *
     * @param publicKey the ECPublicKey.
     * @return a new PublicKeySource.
     */
    PublicKeySource fromECPublicKey(ECPublicKey publicKey);

}
