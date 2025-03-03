package org.jrl.crypto;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密解密工具类
 *
 * @author JerryLong
 */
public class JrlAesUtil {
    /**
     * 秘钥长度
     */
    private static final Integer SECRET_KEY_LENGTH = 16;

    /**
     * 解密 NoPadding
     *
     * @param encryptStr 加密字符串
     * @param secretKey  秘钥
     * @return 明文
     */
    public static String decryptNoPadding(String encryptStr, String secretKey) throws Exception {
        SecretKeySpec skey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skey);
        String res = new String(cipher.doFinal(Base64.getDecoder().decode(encryptStr.getBytes(StandardCharsets.UTF_8))));
        if (StringUtils.isNotBlank(res)) {
            return res.replace(secretKey, "");
        }
        throw new RuntimeException("decryptNoPadding fail !");
    }

    /**
     * 解密 PKCS5Padding
     *
     * @param encryptStr 加密字符串
     * @param secretKey  秘钥
     * @return 明文
     */
    public static String decrypt(String encryptStr, String secretKey) throws Exception {
        SecretKeySpec skey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skey);
        String res = new String(cipher.doFinal(Base64.getDecoder().decode(encryptStr.getBytes(StandardCharsets.UTF_8))));
        if (StringUtils.isNotBlank(res)) {
            return res.replace(secretKey, "");
        }
        throw new RuntimeException("decrypt fail !");
    }

    /**
     * 开放平台网关AES加密
     *
     * @param text      明文字符串
     * @param secretKey 秘钥
     * @return 加密后的字符串
     */
    public static String encryptWithPadding(String text, String secretKey) throws Exception {
        if (secretKey.length() != SECRET_KEY_LENGTH) {
            throw new IllegalArgumentException("secretKey length must be 16");
        }
        SecretKeySpec skey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        return new String(Base64.getEncoder().encode(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8))));
    }
}
