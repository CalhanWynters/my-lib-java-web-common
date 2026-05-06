package com.github.calhanwynters.domain.validationchecks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record ProfanityList(Set<String> prohibitedWords) {
    public ProfanityList {
        if (prohibitedWords == null) throw new IllegalArgumentException("Prohibited words cannot be null.");
        prohibitedWords = Set.copyOf(prohibitedWords);
    }

    /**
     * Searches for any prohibited word within the given text.
     * Returns an Optional containing the first word found, or empty if clean.
     */
    public Optional<String> findFirstIn(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String normalized = text.toLowerCase();

        return prohibitedWords.stream()
                .filter(normalized::contains)
                .findFirst();
    }

    // Method to add a prohibited word; returns a new ProfanityList instance
    public ProfanityList addProhibitedWord(String word) {
        Set<String> updatedSet = new HashSet<>(prohibitedWords); // Create a mutable copy
        updatedSet.add(word.toLowerCase());
        return new ProfanityList(updatedSet); // Return a new instance with the updated set
    }

    // Method to check if a word is in the profanity list (case-insensitive)
    public boolean contains(String word) {
        return prohibitedWords.contains(word.toLowerCase());
    }

    // Provide an unmodifiable view of the set of prohibited words
    public Set<String> getProhibitedWords() {
        return Collections.unmodifiableSet(prohibitedWords);
    }

    // Factory method for creating an empty profanity list
    public static ProfanityList empty() {
        return new ProfanityList(Collections.emptySet());
    }
}
