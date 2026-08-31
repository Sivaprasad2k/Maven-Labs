package com.shevay.knowledge.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class providing deterministic SHA-256 hashing for document identity,
 * content integrity, and chunk identity.
 */
public final class HashUtil {

    private HashUtil() {
        // Private constructor for utility class
    }

    /**
     * Computes the SHA-256 hexadecimal string representation of the provided text.
     *
     * @param input UTF-8 string to hash
     * @return 64-character lowercase hex string
     */
    public static String sha256(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string for SHA-256 cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Normalizes a relative file path to use forward slashes ('/') consistently
     * across operating systems (Windows vs Linux/macOS).
     *
     * @param path raw relative path
     * @return normalized relative path
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
