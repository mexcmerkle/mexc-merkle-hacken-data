package com.mexc.merkle.data.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 utility class
 */
public class MD5Util {
    
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * Calculate MD5 hash of file
     * @param file File
     * @return MD5 hash
     * @throws IOException IO exception
     * @throws NoSuchAlgorithmException Algorithm exception
     */
    public static String calculateFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] digest = md5.digest();
        return bytesToHex(digest);
    }

    /**
     * Calculate MD5 hash of string value
     * @param value String value
     * @return MD5 hash
     * @throws NoSuchAlgorithmException Algorithm exception
     */
    public static String calculateValueMD5(String value) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(value.getBytes());
        byte[] digest = md5.digest();
        return bytesToHex(digest);
    }
    
    /**
     * Convert byte array to hexadecimal string
     * @param bytes Byte array
     * @return Hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

}
