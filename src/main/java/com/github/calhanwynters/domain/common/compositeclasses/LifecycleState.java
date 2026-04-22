package com.github.calhanwynters.domain.common.compositeclasses;

/**
 * Immutable Value Object for Aggregate Lifecycle State.
 */
public record LifecycleState(boolean archived, boolean softDeleted) {

    // Records are non-null by nature for primitives,
    // but we can add logic here if needed.

    /**
     * Wither for Archived status.
     * Returns a new instance with the updated flag.
     */
    public LifecycleState withArchived(boolean archived) {
        return new LifecycleState(archived, this.softDeleted);
    }

    /**
     * Wither for Soft Deleted status.
     * Returns a new instance with the updated flag.
     */
    public LifecycleState withSoftDeleted(boolean softDeleted) {
        return new LifecycleState(this.archived, softDeleted);
    }

    /**
     * Helper for domain guards.
     */
    public boolean isActive() {
        return !archived && !softDeleted;
    }
}
