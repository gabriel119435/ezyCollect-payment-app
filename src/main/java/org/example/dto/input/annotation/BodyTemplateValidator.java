package org.example.dto.input.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.service.TemplateRenderer;
import org.springframework.util.StringUtils;

public class BodyTemplateValidator implements ConstraintValidator<ValidBodyTemplate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // @NotEmpty @NotBlank will handle
        if (!StringUtils.hasText(value)) return true;

        try {
            TemplateRenderer.validateTemplate(value);
            return true;
        } catch (IllegalArgumentException e) {
            // custom validation message from exception
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                    .addConstraintViolation();
            return false;
        }
    }
}