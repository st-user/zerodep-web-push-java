package com.zerodeplibs.webpush;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;


public class JCAProviderInitializer {

    public static void initialize() {

        loadProviderIfPresent("org.bouncycastle.jce.provider.BouncyCastleProvider");
        showProviders();
    }


    private static void showProviders() {
        try {
            System.out.println(new SecureRandom().getProvider());
            System.out.println(KeyFactory.getInstance("EC").getProvider());
            System.out.println(KeyPairGenerator.getInstance("EC").getProvider());
            System.out.println(KeyAgreement.getInstance("ECDH").getProvider());
            System.out.println(Mac.getInstance("HmacSHA256").getProvider());
            System.out.println(Cipher.getInstance("AES/GCM/NoPadding").getProvider());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadProviderIfPresent(String name) {
        try {
            Provider p = (Provider) Class.forName(name).getDeclaredConstructor().newInstance();
            Security.insertProviderAt(p, 1);
        } catch (Throwable e) {
            System.out.println(name + " not found.");
        }
    }

}
