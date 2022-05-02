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
    private String aesKey;
    private static final String ALGORITHMS = "AES/ECB/PKCS5Padding";
    private Cipher encryptCipher;
    private Cipher decryptCipher;


    public AESUtil() {
        this("0123456789abcdef");
    }

    public AESUtil(String aesKey) {
        this.aesKey = aesKey;
        init();
    }

    public void init() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        keyGenerator.init(128);     // 设置密钥长度为128
        try {
            encryptCipher = Cipher.getInstance(ALGORITHMS);   // 创建密码器
            encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return;
        }
        try {
            decryptCipher = Cipher.getInstance(ALGORITHMS);   // 创建密码器
            decryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey.getBytes(), "AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    // 加密
    public byte[] encrypt(byte[] src){
        byte[] bytes = null;
        try {
            bytes =  encryptCipher.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }
    public byte[] encrypt(byte[] src, int pos, int length){
        byte[] bytes = null;
        try {
            bytes =  encryptCipher.doFinal(src, pos, length);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // 解密
    public byte[] decrypt(byte[] src){
        byte[] bytes = null;
        try {
            bytes =  decryptCipher.doFinal(src);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public byte[] decrypt(byte[] src, int pos, int length){
        byte[] bytes = null;
        try {
            bytes =  decryptCipher.doFinal(src, pos, length);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public int countPadding(int src){    // 补全不足16整数倍的数 因为128位加密需要位数是128整数倍的字节数组，一个字节8位 8*16=128
        return (src + 16) / 16 * 16;
    }

    public static void main(String[] args) {
        String srcString = "dasfasjkfhjashdfjhausdfhuksaddafahsjkdfhjashdfjkajdfhakjhfdd5454524asd521f5as4df35sa4f";
        byte[] src = srcString.getBytes();
        byte[] encrypted = new byte[0];
        AESUtil aesUtil = new AESUtil();
        encrypted = aesUtil.encrypt(src, 2, 16);
        byte[] decrypted = new byte[0];
        decrypted = aesUtil.decrypt(encrypted);
        System.out.println(src.length+" "+encrypted.length+" "+decrypted.length);
        String s = new String(decrypted);
        System.out.println(s);
        System.out.println(s.equals(srcString));
    }
}
