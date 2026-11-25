package com.neura.loanmanagement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidTenureValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTenure {
    String message() default "Tenure must be between 6 and 360 months";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min() default 6;
    int max() default 360;
}
