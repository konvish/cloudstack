package com.kong.cloudstack.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
/**
 * Created by kong on 2016/1/24.
 */
public class AESUtil {
    public AESUtil() {
    }

    public static void main(String[] args) throws Exception {
        String content = "我爱你";
        System.out.println("加密前：" + content);
        String key = "123456";
        System.out.println("加密密钥和解密密钥：" + key);
        String encrypt = aesEncrypt(content, key);
        System.out.println("加密后：" + encrypt);
        String decrypt = aesDecrypt(encrypt, key);
        System.out.println("解密后：" + decrypt);
    }

    public static String binary(byte[] bytes, int radix) {
        return (new BigInteger(1, bytes)).toString(radix);
    }

    public static String base64Encode(byte[] bytes) {
        return (new BASE64Encoder()).encode(bytes);
    }

    public static byte[] base64Decode(String base64Code) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(base64Code);
    }

    public static byte[] md5(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        return md.digest();
    }

    public static byte[] md5(String msg) throws Exception {
        return md5(msg.getBytes());
    }

    public static String md5Encrypt(String msg) throws Exception {
        return base64Encode(md5(msg));
    }

    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(encryptKey.getBytes());
        kgen.init(128, secureRandom);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(1, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        return cipher.doFinal(content.getBytes("utf-8"));
    }

    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(decryptKey.getBytes());
        kgen.init(128, secureRandom);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(2, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
    }
}