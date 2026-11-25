package com.neura.loanmanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTenureValidator implements ConstraintValidator<ValidTenure, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidTenure constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value >= min && value <= max;
    }
}
