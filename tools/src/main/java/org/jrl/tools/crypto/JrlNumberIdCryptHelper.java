package org.jrl.tools.crypto;

import org.jrl.tools.utils.JrlMd5;
import org.jrl.tools.log.JrlLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 数字身份标识工具类
 *
 * @author JerryLong
 */
class JrlNumberIdCryptHelper {
    /**
     * 作用域，每个作用域生成一个md5字典
     */
    private final String scope;
    /**
     * 前缀
     */
    private final String prefix;
    /**
     * 加密后固定长度
     */
    private final int encryptedFixedLength;
    /**
     * 随机数最小长度
     */
    private final int numberMinLength;
    /**
     * 随机数字典
     */
    private final String dictionaries;
    /**
     * 密钥，使用16位长度，aes加密
     */
    private final String secretKey;

    private static final String JOINNER_STR = "_";
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlNumberIdCryptHelper.class);

    public JrlNumberIdCryptHelper(String scope, String prefix, int encryptedFixedLength, int numberMinLength, String secretKey) {
        this.scope = scope;
        this.prefix = prefix;
        //对scope与prefix 生成一个md5字典
        this.dictionaries = JrlMd5.dm5(this.scope + prefix);
        this.encryptedFixedLength = encryptedFixedLength;
        this.numberMinLength = numberMinLength;
        this.secretKey = secretKey;
    }

    /**
     * 对数字id字符串进行加密
     *
     * @param nid 一个由数字组成的id字符串，长度不能低于设置的numberMinLength
     * @return 加密出来的固定前缀固定长度字符串
     */
    public String encrypt(String nid) {
        if (nid.length() < this.numberMinLength) {
            throw new IllegalArgumentException("nid 长度不能低于" + this.numberMinLength);
        }
        final StringBuilder openId = new StringBuilder(this.prefix);
        final char[] chars = this.dictionaries.toCharArray();
        for (char c : nid.toCharArray()) {
            final int i = Integer.parseInt(String.valueOf(c));
            final char c1 = getDictionariesIndex(chars, i);
            final char c2 = getDictionariesIndex(chars, i + 1);
            //如果匹配的字符串，有多个，则把uid拼在后面
            if (getIndex(this.dictionaries, String.valueOf(c1) + c2).size() > 1) {
                openId.append(c1).append(c2).append(c);
            } else {
                openId.append(c1).append(c2);
            }
        }
        if (openId.length() > encryptedFixedLength) {
            return openId.toString();
        }
        openId.append(getEncryptStr(nid, this.encryptedFixedLength - openId.length()));
        return openId.toString();
    }

    /**
     * 转换成明文
     *
     * @param jrlId 生成的固定长度id
     * @return
     */
    public String decrypt(String jrlId) {
        if (jrlId.startsWith(this.prefix)) {
            final String str = jrlId.substring(this.prefix.length());
            final StringBuilder nid = new StringBuilder();
            //循环openId，用2位去匹配appKey，拿到匹配结果，单个就直接用
            for (int i = 0; i < str.length(); i++) {
                if (str.length() < i + 2) {
                    break;
                }
                final String substring = str.substring(i, ++i + 1);
                final List<Integer> index = getIndex(this.dictionaries, substring);
                if (index.size() < 1) {
                    break;
                }
                if (index.size() > 1) {
                    i++;
                    if (str.length() <= i) {
                        break;
                    }
                    final char c = str.charAt(i);
                    if (!Character.isDigit(c)) {
                        break;
                    }
                    nid.append(c);
                } else {
                    nid.append(index.get(0));
                }
            }
            if (jrlId.equals(encrypt(nid.toString()))) {
                return nid.toString();
            }
            //循环减一位尝试
            for (int p = nid.length(); p > this.numberMinLength; p--) {
                nid.deleteCharAt(nid.length() - 1);
                if (jrlId.equals(encrypt(nid.toString()))) {
                    return nid.toString();
                }
            }
        }
        throw new RuntimeException();
    }

    private char getDictionariesIndex(char[] chars, int i) {
        if (chars.length > i) {
            return chars[i];
        } else {
            throw new JrlNumberIdCryptUtil.JrlNumberIdCryptException();
        }
    }

    private List<Integer> getIndex(String a, String b) {
        int index = 0;
        final List<Integer> list = new ArrayList<>();
        while ((index = a.indexOf(b, index)) >= 0) {
            list.add(index);
            index++;
        }
        return list;
    }


    private String getEncryptStr(String nid, int length) {
        final String merge = merge(nid);
        String encryptVal = null;
        try {
            encryptVal = JrlAesUtil.encryptWithPadding(merge, this.secretKey);
            if (StringUtils.isBlank(encryptVal)) {
                return null;
            }
            final String s = Base64.encodeBase64String(encryptVal.getBytes(StandardCharsets.UTF_8));
            return s.substring(0, length);
        } catch (Exception e) {
            LOGGER.error("getEncryptStr error, scope:{}, nid:{}, length:{}", this.scope, nid, length, e);
            return null;
        }
    }

    private String merge(String nid) {
        return this.dictionaries + JOINNER_STR + nid;
    }
}