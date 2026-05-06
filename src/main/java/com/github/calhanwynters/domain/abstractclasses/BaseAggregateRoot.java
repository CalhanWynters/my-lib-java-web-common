package com.github.calhanwynters.domain.abstractclasses;

import com.github.calhanwynters.domain.valueobjects.Actor;
import com.github.calhanwynters.domain.compositeclasses.AuditMetadata;
import com.github.calhanwynters.domain.compositeclasses.LifecycleState;
import com.github.calhanwynters.domain.exceptions.DomainAuthorizationException;
import com.github.calhanwynters.domain.validationchecks.DomainGuard;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseAggregateRoot<
        T extends BaseAggregateRoot<T, ID, UUID_TYPE, BUS_UUID>,
        ID,
        UUID_TYPE,
        BUS_UUID
        > extends AbstractAggregateRoot<T> {

    protected ID id;
    protected UUID_TYPE uuId;
    protected BUS_UUID businessUuId;
    protected Long optLockVer;
    protected Integer schemaVersion;
    protected AuditMetadata auditMetadata;
    protected LifecycleState lifecycleState;
    protected OffsetDateTime lastSyncedAt;

    protected BaseAggregateRoot(ID id, UUID_TYPE uuId, BUS_UUID businessUuId,
                                AuditMetadata auditMetadata, LifecycleState lifecycleState,
                                Long optLockVer, Integer schemaVer, OffsetDateTime lastSyncedAt) {
        // Standardizing on the existence check from your library
        this.id = id;
        this.uuId = DomainGuard.notNull(uuId, "Aggregate UUID");
        this.businessUuId = DomainGuard.notNull(businessUuId, "Business Identity");
        this.auditMetadata = DomainGuard.notNull(auditMetadata, "Audit Metadata");
        this.lifecycleState = (lifecycleState != null) ? lifecycleState : LifecycleState.active();

        this.optLockVer = optLockVer;
        this.schemaVersion = (schemaVer != null) ? schemaVer : 1;
        this.lastSyncedAt = (lastSyncedAt != null) ? lastSyncedAt : OffsetDateTime.now(ZoneOffset.UTC);
    }


    protected BaseAggregateRoot() {}

    // --- GENERIC ENGINE & ORCHESTRATORS ---

    protected <V> void applyDomainChange(Actor actor, V newValue, BiFunction<V, Actor, V> validator,
                                         Function<V, Object> eventFactory, Consumer<V> mutation) {
        ensureActive();
        V validatedValue = validator.apply(newValue, actor);
        this.applyChange(actor, eventFactory.apply(validatedValue), () -> mutation.accept(validatedValue));
    }

    public void executeSync(Actor actor, Function<Actor, Object> eventFactory) {
        ensureActive();
        verifySyncAuthority(actor);
        this.applyChange(actor, eventFactory.apply(actor), this::recordSync);
    }

    /**
     * Standardized Business ID Change using Generics.
     */
    public void executeBusinessUuIdUpdate(BUS_UUID newId, Actor actor, Function<BUS_UUID, Object> eventFactory) {
        ensureActive();
        // Since BUS_UUID is generic, we use a specialized validator or cast carefully
        BUS_UUID validatedId = evaluateGenericBusinessIdChange(this.businessUuId, newId, actor);
        this.applyChange(actor, eventFactory.apply(validatedId), () -> this.businessUuId = validatedId);
    }

    // --- LIFECYCLE ACTIONS ---

    public void executeArchive(Actor actor, Object event) {
        verifyLifecycleAuthority(actor);
        this.applyChange(actor, event, () -> this.lifecycleState = this.lifecycleState.withArchived(true));
    }

    public void executeUnarchive(Actor actor, Object event) {
        verifyLifecycleAuthority(actor);
        this.applyChange(actor, event, () -> this.lifecycleState = this.lifecycleState.withArchived(false));
    }

    public void executeSoftDelete(Actor actor, Object event) {
        ensureActive();
        verifyLifecycleAuthority(actor);
        this.applyChange(actor, event, () -> this.lifecycleState = this.lifecycleState.withSoftDeleted(true));
    }

    public void executeRestore(Actor actor, Object event) {
        if (this.lifecycleState == null || !this.lifecycleState.softDeleted()) return;
        verifyRestorable(actor);
        this.applyChange(actor, event, () -> this.lifecycleState = this.lifecycleState.withSoftDeleted(false));
    }

    public void executeHardDelete(Actor actor, Object event) {
        verifyHardDeleteAuthority(actor);
        this.applyChange(actor, event, null);
    }

    // --- GUARDS & AUDIT ---

    protected void ensureActive() {
        if (this.lifecycleState != null && this.lifecycleState.softDeleted()) {
            throw new IllegalStateException("Action not allowed on a soft-deleted aggregate.");
        }
    }

    protected void applyChange(Actor actor, Object event, Runnable mutation) {
        DomainGuard.notNull(actor, "Actor");
        Optional.ofNullable(mutation).ifPresent(Runnable::run);
        this.recordUpdate(actor);
        Optional.ofNullable(event).ifPresent(this::registerEvent);
    }

    protected void recordUpdate(Actor actor) {
        this.auditMetadata = this.auditMetadata.update(actor);
    }

    protected void recordSync() {
        // Truncate to MICROS to match your AuditMetadata standard
        this.lastSyncedAt = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    }


    public boolean isSyncPending() {
        if (lastSyncedAt == null) return true;
        return auditMetadata.lastModified().value().isAfter(lastSyncedAt);
    }

    // --- SOC 2 STATIC VALIDATIONS ---

    public static void verifySyncAuthority(Actor actor) {
        if (!actor.hasRole(Actor.ROLE_MANAGER) && !actor.hasRole(Actor.ROLE_ADMIN)) {
            throw new DomainAuthorizationException("Data sync requires Manager or Admin roles.", "SEC-403", actor);
        }
    }

    public static void verifyLifecycleAuthority(Actor actor) {
        if (!actor.hasRole(Actor.ROLE_MANAGER) && !actor.hasRole(Actor.ROLE_ADMIN)) {
            throw new DomainAuthorizationException("Lifecycle actions require Manager or Admin roles.", "SEC-403", actor);
        }
    }

    public static void verifyRestorable(Actor actor) {
        if (!actor.hasRole(Actor.ROLE_ADMIN)) {
            throw new DomainAuthorizationException("Restoration is restricted to Administrators.", "SEC-403", actor);
        }
    }

    public static void verifyHardDeleteAuthority(Actor actor) {
        if (!actor.hasRole(Actor.ROLE_ADMIN)) {
            throw new DomainAuthorizationException("Hard deletes are restricted to Administrators.", "SEC-001", actor);
        }
    }

    /**
     * Generic version of Business ID Change to handle specialized record types.
     */
    protected BUS_UUID evaluateGenericBusinessIdChange(BUS_UUID current, BUS_UUID next, Actor actor) {
        if (!actor.hasRole(Actor.ROLE_ADMIN)) {
            throw new DomainAuthorizationException("Business ID changes restricted to Admin.", "SEC-401", actor);
        }
        DomainGuard.notNull(next, "New Business UUID");
        if (next.equals(current)) throw new IllegalArgumentException("New ID must be different.");
        return next;
    }

    // --- GETTERS ---
    public ID getId() { return id; }
    public UUID_TYPE getUuId() { return uuId; }
    public BUS_UUID getBusinessUuId() { return businessUuId; }
    public Long getOptLockVer() { return optLockVer; }
    public Integer getSchemaVersion() { return schemaVersion; }
    public OffsetDateTime getLastSyncedAt() { return lastSyncedAt; }
    public AuditMetadata getAuditMetadata() { return auditMetadata; }
    public LifecycleState getLifecycleState() { return this.lifecycleState; }
}
