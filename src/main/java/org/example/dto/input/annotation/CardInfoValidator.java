package org.example.dto.input.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.function.IntUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


public class CardInfoValidator implements ConstraintValidator<ValidCardInfo, String> {

    private static final Pattern CARD_PATTERN = Pattern.compile("^(\\d{4} \\d{4} \\d{4} \\d{4}) (\\d{2}/\\d{2}) (\\d{3})$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // @NotEmpty @NotBlank will handle
        if (!StringUtils.hasText(value)) return true;

        try {
            Matcher m = CARD_PATTERN.matcher(value);
            if (m.matches()) {
                String numberSequence = m.group(1);
                if (!luhnVerification(numberSequence)) throw new IllegalArgumentException("invalid card number");
                if (!validateDate(m.group(2))) throw new IllegalArgumentException("card expired");
                return true;
            } else throw new IllegalArgumentException("invalid card info, format 1234 1234 1234 1234 12/99 123");
        } catch (IllegalArgumentException e) {
            // custom validation message from exception
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

    private static boolean validateDate(String date) {
        int expiryMonth = Integer.parseInt(date.split("/")[0]);
        int expiryYear = Integer.parseInt(date.split("/")[1]);
        int currentYear = LocalDateTime.now().getYear() % 100;
        int currentMonth = LocalDateTime.now().getMonthValue();
        return expiryYear > currentYear || (expiryYear == currentYear && expiryMonth > currentMonth);
    }

    public static boolean luhnVerification(String s) {
        IntUnaryOperator sumDigits = n -> n / 10 + n % 10;
        int[] digits = s.replaceAll("\\s+", "")
                .chars()
                .map(Character::getNumericValue)
                .toArray();

        // value:   [4,2,4,2]
        // indexes: [0,1,2,3]
        return IntStream.rangeClosed(1, digits.length)  // 1,2,3,4
                .map(i -> digits.length - i) // 3,2,1,0
                // checks if index is even to double it. doubles digits[2] and digits[0] only, which have value 4 and 4
                .map(i -> i % 2 == 0 ? sumDigits.applyAsInt(digits[i] * 2) : digits[i])
                .sum() % 10 == 0;
    }
}
