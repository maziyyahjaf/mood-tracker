package com.example.maziyyah.mood_tracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyAndSizeValidator implements ConstraintValidator<NotEmptyAndSize, String> {
    private String notEmptyMessage;
    private String sizeMessage;
    private int min;

    @Override
    public void initialize(NotEmptyAndSize constraintAnnotation) {
        this.notEmptyMessage = constraintAnnotation.notEmptyMessage();
        this.sizeMessage = constraintAnnotation.sizeMessage();
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            // Handle @NotEmpty validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(notEmptyMessage).addConstraintViolation();
            return false;
        } else if (value.length() < min) {
            // Handle @Size validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(sizeMessage).addConstraintViolation();
            return false;
        }
        return true;
    }
}
