package com.sniplink.util;

import java.security.SecureRandom;

public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LENGTH = 7;
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        StringBuilder shortCode = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(BASE62_CHARS.length());
            shortCode.append(BASE62_CHARS.charAt(index));
        }
        return shortCode.toString();
    }

    public static boolean isValidCustomSlug(String slug) {
        if (slug == null || slug.length() < 3 || slug.length() > 20) {
            return false;
        }
        return slug.matches("^[a-zA-Z0-9_-]+$");
    }
}
