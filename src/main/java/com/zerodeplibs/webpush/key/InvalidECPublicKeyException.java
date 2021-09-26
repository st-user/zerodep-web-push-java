package com.zerodeplibs.webpush.key;

/**
 * This Exception is thrown by {@link PublicKeySource} during extraction
 * to indicate that the public key is invalid.
 *
 * <p>
 * The validation method described in Section 5.6.2.3 of <a href="https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-56Ar2.pdf">Recommendation for Pair-Wise Key Establishment Schemes Using Discrete Logarithm Cryptography - NIST Special Publication 800-56A Revision 2</a>
 * is used.
 * </p>
 *
 * @author Tomoki Sato
 * @see PublicKeySource#extract()
 */
public class InvalidECPublicKeyException extends RuntimeException {

    InvalidECPublicKeyException(String message) {
        super(message);
    }
}
