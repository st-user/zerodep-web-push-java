package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPublicKey;

/**
 * <p>
 * Implementations of this interface represent a source of an {@link ECPublicKey}.
 * </p>
 *
 * <p>
 * A PublicKeySource validates the extracted public key depending on its implementation.
 * </p>
 *
 * <p>
 * Typically, to create a PublicKeySource,
 * a factory method defined in {@link PublicKeySources} is used.
 * </p>
 *
 * <div><b>Thread Safety:</b></div>
 * <p>
 * Depends on implementations.
 * Typically, an implementation of this interface is not thread-safe.
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
