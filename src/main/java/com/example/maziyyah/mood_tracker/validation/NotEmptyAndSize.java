package com.example.maziyyah.mood_tracker.validation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = NotEmptyAndSizeValidator.class) // Validator implementation
@Target({ElementType.FIELD}) // Targets fields only
@Retention(RetentionPolicy.RUNTIME) // Retain at runtime
public @interface NotEmptyAndSize { // Use @interface for custom annotations

    String notEmptyMessage(); // Message if field is empty
    String sizeMessage();     // Message if size constraint is violated
    int min();                // Minimum size constraint

    String message() default ""; // Default message (optional, rarely used)

    Class<?>[] groups() default {}; // Grouping for constraints (default empty)

    Class<? extends Payload>[] payload() default {}; // Metadata for additional info
}