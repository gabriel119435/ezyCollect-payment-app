package org.example.dto.input.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class URLValidator implements ConstraintValidator<ValidURL, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // @NotEmpty @NotBlank will handle
        if (!StringUtils.hasText(value)) return true;

        try {
            new URL(value).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            // custom validation message from exception
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}