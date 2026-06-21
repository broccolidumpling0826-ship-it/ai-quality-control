package com.jhict.quality.common.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 敏感信息脱敏（API Key、手机、邮箱等）
 */
public final class MaskUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([a-zA-Z0-9._%+-]{1,3})[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");

    private static final Pattern API_KEY_PATTERN = Pattern.compile("(sk-[a-zA-Z0-9]{4})[a-zA-Z0-9]+");

    private static final int MAX_SUMMARY_LEN = 500;

    private MaskUtils() {
    }

    public static String maskSummary(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String masked = maskApiKey(text);
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("$1***@$2");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("$1****$2");
        if (masked.length() > MAX_SUMMARY_LEN) {
            return masked.substring(0, MAX_SUMMARY_LEN) + "...";
        }
        return masked;
    }

    public static String maskApiKey(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        return API_KEY_PATTERN.matcher(text).replaceAll("$1****");
    }
}
