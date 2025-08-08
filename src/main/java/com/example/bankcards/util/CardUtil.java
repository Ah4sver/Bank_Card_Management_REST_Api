package com.example.bankcards.util;

public class CardUtil {

    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String maskedString = "*".repeat(12) +
                cardNumber.substring(12);

        return maskedString;
    }
}
