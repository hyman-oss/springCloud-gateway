package com.hyzs.dog.gateway.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5工具类
 * @author Hua-cloud
 */
public class MD5Utils {

    public static String encrypt(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = md.digest(password.getBytes());
        return hex(bytes);
    }

    public static String encrypt(String password, String salt) {
        String cryptPwd = encrypt(password);
        System.out.println(cryptPwd+salt);
        return encrypt(cryptPwd + salt);
    }

    public static String encrypt(String password, String salt, String salt2) {
        String cryptPwd = encrypt(password);
        String addSaltPwd = encrypt(cryptPwd, salt);
        return encrypt(addSaltPwd, salt2);

    }

    private static String hex(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

}
