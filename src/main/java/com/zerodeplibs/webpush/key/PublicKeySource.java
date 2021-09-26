package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPublicKey;

/**
 * This class represents a source of an {@link ECPublicKey}.
 *
 * <p>
 * Depending on the implementation, PublicKeySource validates the public key during extraction.
 * </p>
 *
 * <p>
 * Typically, to create a PublicKeySource,
 * a factory method defined in {@link PublicKeySources} is used.
 * </p>
 *
 * @author Tomoki Sato
 * @see PrivateKeySource
 * @see PublicKeySources
 * @see PublicKeySourceFactory
 */
public interface PublicKeySource {

    /**
     * Extracts the {@link ECPublicKey} from the source represented by this object.
     *
     * @return the elliptic curve (EC) public key.
     * @throws InvalidECPublicKeyException when the extracted
     *                                     public key is invalid(Depending on the implementation).
     */
    ECPublicKey extract();

    /**
     * Extracts the byte array of the elliptic curve (EC) public key in uncompressed form.
     *
     * @return the byte array of the public key in uncompressed form.
     */
    byte[] extractUncompressedBytes();
}
