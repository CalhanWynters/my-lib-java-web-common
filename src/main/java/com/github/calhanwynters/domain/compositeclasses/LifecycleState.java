package com.github.calhanwynters.domain.compositeclasses;

/**
 * Immutable Value Object for Aggregate Lifecycle State.
 */
public record LifecycleState(boolean archived, boolean softDeleted) {

    // Records are non-null by nature for primitives,
    // but we can add logic here if needed.

    public LifecycleState {
        // Semantic Rule: An item shouldn't usually be 'archived' and 'deleted' simultaneously.
        // This keeps your search filters (isActive) predictable.
        com.github.calhanwynters.domain.validationchecks.DomainGuard.ensure(
                !(archived && softDeleted),
                "An entity cannot be both archived and soft-deleted.",
                "VAL-011", "SEMANTIC_CONFLICT"
        );
    }

    public static LifecycleState active() {
        return new LifecycleState(false, false);
    }

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
