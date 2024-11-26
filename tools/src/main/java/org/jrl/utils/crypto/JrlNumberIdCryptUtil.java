package org.jrl.utils.crypto;

import org.jrl.utils.JrlLruCache;

import java.util.Map;

/**
 * 数字ID加密工具
 * 通过字典映射，将数字字符串转换为固定长度的加密字符串
 *
 * @author JerryLong
 */
public class JrlNumberIdCryptUtil {
    /**
     * 加密缓存
     */
    private static final Map<String, JrlNumberIdCryptHelper> HELPER_MAP = new JrlLruCache<>();

    /**
     * 加密
     *
     * @param scope                域
     * @param prefix               前缀
     * @param secretKey            密钥
     * @param nid                  待加密数字ID
     * @return 加密后的字符串
     */
    public static String encrypt(String scope, String prefix, String secretKey, String nid) {
        return HELPER_MAP.computeIfAbsent(scope + prefix, e -> new JrlNumberIdCryptHelper(scope, prefix, 48, 6, secretKey)).encrypt(nid);
    }

    /**
     * 解密
     *
     * @param scope                域
     * @param prefix               前缀
     * @param secretKey            密钥
     * @param jrlId                待解密加密字符串
     * @return 解密后的数字ID
     */
    public static String decrypt(String scope, String prefix, String secretKey, String jrlId) {
        return HELPER_MAP.computeIfAbsent(scope + prefix, e -> new JrlNumberIdCryptHelper(scope, prefix,48 , 6, secretKey)).decrypt(jrlId);
    }

    /**
     * 加密
     *
     * @param scope                域
     * @param prefix               前缀
     * @param encryptedFixedLength 加密后固定长度
     * @param numberMinLength      数字最小长度
     * @param secretKey            密钥
     * @param nid                  待加密数字ID
     * @return 加密后的字符串
     */
    public static String encrypt(String scope, String prefix, int encryptedFixedLength, int numberMinLength, String secretKey, String nid) {
        return HELPER_MAP.computeIfAbsent(scope + prefix, e -> new JrlNumberIdCryptHelper(scope, prefix, encryptedFixedLength, numberMinLength, secretKey)).encrypt(nid);
    }

    /**
     * 解密
     *
     * @param scope                域
     * @param prefix               前缀
     * @param encryptedFixedLength 加密后固定长度
     * @param numberMinLength      数字最小长度
     * @param secretKey            密钥
     * @param jrlId                待解密加密字符串
     * @return 解密后的数字ID
     */
    public static String decrypt(String scope, String prefix, int encryptedFixedLength, int numberMinLength, String secretKey, String jrlId) {
        return HELPER_MAP.computeIfAbsent(scope + prefix, e -> new JrlNumberIdCryptHelper(scope, prefix, encryptedFixedLength, numberMinLength, secretKey)).decrypt(jrlId);
    }

    public static class JrlNumberIdCryptException extends RuntimeException {
    }
}
