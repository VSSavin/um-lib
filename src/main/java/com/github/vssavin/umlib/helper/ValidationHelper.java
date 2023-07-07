package com.github.vssavin.umlib.helper;

import java.util.regex.Pattern;

/**
 * @author vssavin on 08.01.2022
 */
public final class ValidationHelper {

    private ValidationHelper() {
    }

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmail(String emailStr) {
        return VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr).matches();
    }

    public static boolean isValidPassword(Pattern validRegexPattern, String passwordStr) {
        return validRegexPattern.matcher(passwordStr).matches();
    }
}
