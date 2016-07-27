package com.kong.cloudstack.sz.util;

import com.kong.cloudstack.utils.AESUtil;
/**
 * 加密工具
 * Created by kong on 2016/1/24.
 */
public class EncryptUtil {
    private static final byte[] encryptTag = new byte[]{(byte)83, (byte)1};
    private static final char oneChar = '\u0001';
    public static final String encryptStr = "S\u0001";
    public static final String encryptKey = "i love change";

    public EncryptUtil() {
    }

    public static boolean isEncrypt(byte[] datas) {
        return datas[0] == encryptTag[0] && datas[1] == encryptTag[1];
    }

    public static String encrypt(String cryptStr) throws Exception {
        return "S\u0001" + AESUtil.aesEncrypt(cryptStr, "i love change");
    }

    public static String decrypt(String encryptStr) throws Exception {
        String realEncryptStr = encryptStr.substring(2);
        return AESUtil.aesDecrypt(realEncryptStr, "i love change");
    }
}