package com.DecisionEngine.backend.util;

public class MaskingUtil {

    public static String maskPersonalCode(String code) {
        if (code == null || code.length() < 4) {
            return "****";
        }
        return "****" + code.substring(code.length() - 4);
    }
}
