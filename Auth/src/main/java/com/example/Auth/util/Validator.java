package com.example.Auth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class Validator {
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String PHONE_NUMBER_REGEX =
            "^(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}$";

    private static final Pattern EMAIL = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PHONE = Pattern.compile(PHONE_NUMBER_REGEX);
    public static boolean isValidEmail(String email) {
        Matcher matcher = EMAIL.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Matcher matcher = PHONE.matcher(phoneNumber);
        return matcher.matches();
    }
}
