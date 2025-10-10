package org.example.dto.input.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BodyTemplateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBodyTemplate {

    String message() default "invalid template";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}