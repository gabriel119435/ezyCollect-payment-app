package org.example.dto.input.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = URLValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidURL {
    String message() default "invalid url" ;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}