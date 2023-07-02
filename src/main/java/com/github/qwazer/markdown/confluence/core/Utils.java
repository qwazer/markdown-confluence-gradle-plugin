package com.github.qwazer.markdown.confluence.core;

public class Utils {

    private Utils() {}

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

}
