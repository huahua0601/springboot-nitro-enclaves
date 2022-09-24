package com.awsdemo.springbootkmsdecryptserver.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class AES {
    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static void setKey(final String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(final String strToEncrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Error while encrypting: ", e);
        }
        return null;
    }

    public static String decrypt(final String strToDecrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder()
                    .decode(strToDecrypt)));
        } catch (Exception e) {
            log.error("Error while decrypting: ", e);
        }
        return null;
    }


    public static void main(String[] args) {
        String en = AES.encrypt("123", "JrejnspGcEcebBw4sGd5mMin9+azCUiWC/lIJ8vJu3I=");
        String de = AES.decrypt("uI/INN90EIVow+JQEfilNIwr6xAjehPem/7nOuftG5dVd08ZHw5HGkxSktPVElFxgk2G3vNlbyjH5pB9kDNyKD1Rczn9eFdRlpc05kk0FNEMaEEQoLzD0OTkTlui770C", "cMwKxi/6d7Ju3JPiD6qBB01AE8JKEhrzItXuhGw2GHw=");
        System.out.printf(de);
    }
}
