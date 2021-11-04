package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPublicKey;

/**
 * <p>
 * This class represents a source of an {@link ECPublicKey}.
 * </p>
 *
 * <p>
 * Depending on the implementation,
 * a PublicKeySource validates the public key that is extracted from the source.
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
 */
public interface PublicKeySource {

    /**
     * Extracts the {@link ECPublicKey} from the source represented by this object.
     *
     * @return the elliptic curve (EC) public key.
     * @throws InvalidECPublicKeyException if the extracted
     *                                     public key is invalid(Depending on the implementation).
     */
    ECPublicKey extract();

    /**
     * Extracts the octet sequence of the elliptic curve (EC) public key in uncompressed form.
     *
     * @return the octet sequence of the public key in uncompressed form.
     * @throws InvalidECPublicKeyException if the extracted
     *                                     public key is invalid(Depending on the implementation).
     */
    byte[] extractBytesInUncompressedForm();
}
