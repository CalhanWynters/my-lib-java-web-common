package com.github.calhanwynters.domain.validationchecks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record AllowedList(Set<String> allowedValues) {

    public AllowedList {
        // Ensure the set of allowed values is not null and create an unmodifiable copy
        if (allowedValues == null) {
            throw new IllegalArgumentException("Allowed values cannot be null.");
        }
        allowedValues = Set.copyOf(allowedValues); // Create an unmodifiable set
    }

    // Method to check if a value is in the allowed list (case-sensitive)
    public boolean contains(String value) {
        return allowedValues.contains(value);
    }

    // Provide an unmodifiable view of the set of allowed values
    public Set<String> getAllowedValues() {
        return Collections.unmodifiableSet(allowedValues);
    }

    // Factory method for creating an empty allowed list
    public static AllowedList empty() {
        return new AllowedList(Collections.emptySet());
    }

    // Method to add a new allowed value; returns a new AllowedList instance
    public AllowedList addAllowedValue(String value) {
        Set<String> updatedSet = new HashSet<>(allowedValues); // Create a mutable copy
        updatedSet.add(value);
        return new AllowedList(updatedSet); // Return a new instance with the updated set
    }
}
