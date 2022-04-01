package dev.overwave.whereeat.util;

public class StringUtils {
    private StringUtils() {
    }

    public static String trimWithEllipsis(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 1) + "…";
        }
        return text;
    }
}