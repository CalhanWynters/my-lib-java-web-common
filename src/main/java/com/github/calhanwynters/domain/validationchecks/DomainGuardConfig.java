package com.github.calhanwynters.domain.validationchecks;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class DomainGuardConfig {

    // Configurable parameters
    private Duration temporalTolerance;
    private ProfanityList profanityList; // Change from Set<String> to ProfanityList
    private AllowedList allowedList;
    private int minLength;
    private int maxLength;
    private int maxScale;
    private int minCollectionSize;
    private int maxCollectionSize;

    // Constructor with default values
    public DomainGuardConfig() {
        this.temporalTolerance = Duration.ofMillis(500); // Default value
        this.profanityList = ProfanityList.empty(); // Initialize with an empty list
        this.allowedList = AllowedList.empty();
        this.minLength = 5; // Default minimum length
        this.maxLength = 20; // Default maximum length
        this.maxScale = 2; // Default maximum decimal places
        this.minCollectionSize = 1; // Default minimum size
        this.maxCollectionSize = 100; // Default maximum size
    }

    // Getters and Setters
    public Duration getTemporalTolerance() {
        return temporalTolerance;
    }

    public void setTemporalTolerance(Duration temporalTolerance) {
        this.temporalTolerance = temporalTolerance;
    }

    public AllowedList getAllowedList() {
        return allowedList;
    }

    public void setAllowedList(AllowedList allowedList) {
        this.allowedList = allowedList;
    }

    // Method to add an allowed value
    public void addAllowedValue(String value) {
        allowedList = allowedList.addAllowedValue(value);
    }

    public ProfanityList getProfanityList() {
        return profanityList;
    }

    public void setProfanityList(ProfanityList profanityList) {
        this.profanityList = profanityList;
    }

    // Method to add a word to the profanity list
    // Adjusted addProfanity method in DomainGuardConfig
    public void addProfanity(String word) {
        profanityList = profanityList.addProhibitedWord(word.toLowerCase());
    }


    // Method to remove a word from the profanity list
    public void removeProfanity(String word) {
        Set<String> currentWords = new HashSet<>(profanityList.getProhibitedWords());
        currentWords.remove(word.toLowerCase());
        profanityList = new ProfanityList(currentWords);
    }

    // Method to clear the profanity list
    public void clearProfanityList() {
        profanityList = ProfanityList.empty();
    }


    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(int maxScale) {
        this.maxScale = maxScale;
    }

    public int getMinCollectionSize() {
        return minCollectionSize;
    }

    public void setMinCollectionSize(int minCollectionSize) {
        this.minCollectionSize = minCollectionSize;
    }

    public int getMaxCollectionSize() {
        return maxCollectionSize;
    }

    public void setMaxCollectionSize(int maxCollectionSize) {
        this.maxCollectionSize = maxCollectionSize;
    }

    // Optional: A method to reset to default values
    public void resetToDefaults() {
        this.temporalTolerance = Duration.ofMillis(500);
        this.minLength = 5;
        this.maxLength = 20;
        this.maxScale = 2;
        this.minCollectionSize = 1;
        this.maxCollectionSize = 100;
    }
}
