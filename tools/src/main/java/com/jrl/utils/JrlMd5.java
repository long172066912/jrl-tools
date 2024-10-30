package com.jrl.utils;

import com.jrl.utils.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * md5工具
 *
 * @author JerryLong
 */
public class JrlMd5 {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlMd5.class);

    /**
     * MD5
     *
     * @param str 明文
     * @return md5字符串
     */
    public static String dm5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Exception e) {
            LOGGER.error(String.format("string2Md5 error |%s", e));
            return null;
        }
    }
}
