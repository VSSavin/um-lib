package com.github.vssavin.umlib.config;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * @author vssavin on 01.07.2023
 */
public class UmConfigurerTest {

    @Test
    public void passwordValid() {
        int minPasswordLength = 5;
        UmConfigurer umConfigurer = initUmConfigurer(minPasswordLength, false, false, false, false);

        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("11111").matches(),
                String.format("Password length < %s", minPasswordLength));

        umConfigurer = initUmConfigurer(minPasswordLength, true, false, false, false);
        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("abc1def").matches(),
                String.format("Password isn't match pattern: %s", umConfigurer.getPasswordPattern()));

        umConfigurer = initUmConfigurer(minPasswordLength, false, true, false, false);
        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("123a45").matches(),
                String.format("Password doesn't match the pattern: %s", umConfigurer.getPasswordPattern()));

        umConfigurer = initUmConfigurer(minPasswordLength, false, false, true, false);
        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("453U21").matches(),
                String.format("Password doesn't match the pattern: %s", umConfigurer.getPasswordPattern()));

        umConfigurer = initUmConfigurer(minPasswordLength, false, false, false, true);
        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("112#654").matches(),
                String.format("Password doesn't match the pattern: %s", umConfigurer.getPasswordPattern()));

        umConfigurer = initUmConfigurer(minPasswordLength, true, true, true, true);
        Assertions.assertTrue(umConfigurer.getPasswordPattern().matcher("aBc1$").matches(),
                String.format("Password doesn't match the pattern: %s", umConfigurer.getPasswordPattern()));
    }

    @Test
    public void passwordNotValid() {
        int minPasswordLength = 5;
        UmConfigurer umConfigurer = initUmConfigurer(minPasswordLength, true, true, true, true);
        Assertions.assertFalse(umConfigurer.getPasswordPattern().matcher("lksajdfhkjdh").matches(),
                String.format("Password doesn't match the pattern: %s", umConfigurer.getPasswordPattern()));
    }

    private UmConfigurer initUmConfigurer(int passwordMinLength, boolean atLeastOneDigit,
            boolean atLeastOneLowerCaseLatin, boolean atLeastOneUpperCaseLatin, boolean atLeastOneSpecialCharacter) {
        UmConfigurer umConfigurer = new UmConfigurer();
        umConfigurer.passwordConfig()
            .minLength(passwordMinLength)
            .atLeastOneDigit(atLeastOneDigit)
            .atLeastOneLowerCaseLatin(atLeastOneLowerCaseLatin)
            .atLeastOneUpperCaseLatin(atLeastOneUpperCaseLatin)
            .atLeastOneSpecialCharacter(atLeastOneSpecialCharacter);
        return umConfigurer;
    }

}
