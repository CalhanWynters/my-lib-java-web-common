package com.github.calhanwynters.domain.common.compositeclasses;

import com.github.calhanwynters.domain.common.valueobjects.Actor;
import com.github.calhanwynters.domain.common.valueobjects.CreatedAt;
import com.github.calhanwynters.domain.common.valueobjects.LastModified;
import com.github.calhanwynters.domain.common.validationchecks.DomainGuard;

/**
 * Pure Domain Value Object for auditing.
 * No JPA/Infrastructure dependencies allowed here.
 */
public record AuditMetadata(
        CreatedAt createdAt,
        LastModified lastModified,
        Actor lastModifiedBy
) {
    public AuditMetadata {
        DomainGuard.notNull(createdAt, "Created At");
        DomainGuard.notNull(lastModified, "Last Modified");
        DomainGuard.notNull(lastModifiedBy, "Last Modified By");

        DomainGuard.ensure(
                !lastModified.value().isBefore(createdAt.value()),
                "Last modified date (%s) cannot be earlier than creation date (%s)."
                        .formatted(lastModified.value(), createdAt.value()),
                "VAL-016", "TEMPORAL_INTEGRITY"
        );
    }

    public static AuditMetadata create(Actor actor) {
        CreatedAt now = CreatedAt.now();
        return new AuditMetadata(now, new LastModified(now.value()), actor);
    }

    public AuditMetadata update(Actor actor) {
        return new AuditMetadata(this.createdAt, LastModified.now(), actor);
    }

    public static AuditMetadata reconstitute(CreatedAt createdAt, LastModified lastModified, Actor lastModifiedBy) {
        return new AuditMetadata(createdAt, lastModified, lastModifiedBy);
    }
}
