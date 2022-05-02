package com.liang.common;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description: AES加密解密算法
 * @Author: LiangYang
 * @Date: 2022/5/1 下午9:18
 **/
public class AESUtil {
    private static String aesKey = "0123456789abcdef";
    private static final String ALGORITHMS = "AES/ECB/PKCS5Padding";
    private static Cipher encryptCipher;
    private static Cipher decryptCipher;
    public AESUtil instance;
    static {
        init();
    }

    public static boolean setAESKey(String aesKey) {
        AESUtil.aesKey = aesKey;
        return init();
    }
    public static boolean init() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        keyGenerator.init(128);     // 设置密钥长度为128
        try {
            encryptCipher = Cipher.getInstance(ALGORITHMS);   // 创建密码器
            encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
        try {
            decryptCipher = Cipher.getInstance(ALGORITHMS);   // 创建密码器
            decryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    // 加密
    public static byte[] encrypt(byte[] src){
        byte[] bytes = null;
        try {
            bytes =  encryptCipher.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // 解密
    public static byte[] decrypt(byte[] src){
        byte[] bytes = null;
        try {
            bytes =  decryptCipher.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static int countPadding(int src){    // 补全不足16整数倍的数 因为128位加密需要位数是128整数倍的字节数组，一个字节8位 8*16=128
        return (src + 16) / 16 * 16;
    }

    public static void main(String[] args) {
        String srcString = "dasfasjkfhjashdfjhausdfhuksaddafahsjkdfhjashdfjkajdfhakjhfdd5454524asd521f5as4df35sa4f";
        byte[] src = srcString.getBytes();
        byte[] encrypted = new byte[0];
        encrypted = AESUtil.encrypt(src);
        byte[] decrypted = new byte[0];
        decrypted = AESUtil.decrypt(encrypted);
        System.out.println(src.length+" "+encrypted.length+" "+decrypted.length);
        String s = new String(decrypted);
        System.out.println(s);
        System.out.println(s.equals(srcString));
    }
}
