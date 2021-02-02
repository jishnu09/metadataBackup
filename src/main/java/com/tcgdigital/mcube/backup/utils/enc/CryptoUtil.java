package com.tcgdigital.mcube.backup.utils.enc;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtil {

    public static Logger log = LoggerFactory.getLogger(CryptoUtil.class);

    public static String getEncKey(String sessionId) {

        StringBuilder encKey = new StringBuilder("");
        int i = 0;
        for (char c : sessionId.toCharArray()) {
            if (i < 16) {
                encKey.append(c);
                i++;
            }
        }

        return encKey.toString();
    }

    public static String encrypt(String text, String key) {
        if (log.isDebugEnabled()) {
            log.debug("[Crypto.encrypt] \t Execution start ");
        }

        String encryptedString = null;
        try {
            if (!key.isEmpty() && key.length() == 16) {
                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] cipherText = cipher.doFinal(text.getBytes("UTF8"));
                encryptedString = new String(Base64.getEncoder().encode(cipherText), "UTF-8");
            } else {
                throw new Exception("Invalid key or key length is not 16");

            }
        } catch (Exception exception) {
            log.error("[Crypto.encrypt] \t Error occoured during encryption \t ", exception);
        }
        if (log.isDebugEnabled()) {
            log.debug("[Crypto.encrypt] \t Execution end ");
        }

        return encryptedString;
    }

    public static String decrypt(String encryptedText, String key) {
        if (log.isDebugEnabled()) {
            log.debug("[Crypto.decrypt] \t Execution start");
        }

        String decryptedString = null;
        try {
            if (!key.isEmpty() && key.length() == 16) {
                // Create key and cipher
                Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
                Cipher cipher = Cipher.getInstance("AES");

                // decrypt the text
//				byte[] encrypted = encryptedText.getBytes();
                byte[] encrypted =  Base64.getDecoder().decode(encryptedText);
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                decryptedString = new String(cipher.doFinal(encrypted));
            } else {
                throw new Exception("Invalid key or key length is not 16");
            }
        } catch (Exception exception) {
            log.error("[Crypto.decrypt] \t Error occoured during decryption \t ", exception);
        }
        if (log.isDebugEnabled()) {
            log.debug("[Crypto.decrypt] \t Execution End");
        }

        return decryptedString;
    }

}
