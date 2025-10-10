package org.example.dto.input.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CardInfoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCardInfo {
    String message() default "invalid card info";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}