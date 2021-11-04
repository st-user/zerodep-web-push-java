package com.zerodeplibs.webpush.key;

import java.security.interfaces.ECPrivateKey;

/**
 * <p>
 * This class represents a source of an {@link ECPrivateKey}.
 * </p>
 *
 * <p>
 * Typically, to create a PrivateKeySource,
 * a factory method defined in {@link PrivateKeySources} is used.
 * </p>
 *
 * @author Tomoki Sato
 * @see PublicKeySource
 * @see PrivateKeySources
 */
public interface PrivateKeySource {

    /**
     * Extracts the {@link ECPrivateKey} from the source represented by this object.
     *
     * @return the elliptic curve (EC) private key.
     */
    ECPrivateKey extract();
}
