package com.eduplan.voucher.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "vouchers", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherDuration duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status = VoucherStatus.UNUSED;

    @Column(nullable = false)
    private boolean singleUse = true;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant connectedAt;

    private Instant expiresAt;

    private Instant disconnectedAt;

    /** Simulated client identifier (e.g. device name/MAC) supplied at connect time. */
    private String clientIdentifier;

    /** True when an admin force-disconnected an active session, as opposed to natural expiry. */
    @Column(nullable = false)
    private boolean forceDisconnected = false;

    @Version
    private Long version;

    protected Voucher() {
        // for JPA
    }

    public Voucher(String code, VoucherDuration duration, boolean singleUse) {
        this.code = code;
        this.duration = duration;
        this.singleUse = singleUse;
        this.status = VoucherStatus.UNUSED;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public VoucherDuration getDuration() {
        return duration;
    }

    public VoucherStatus getStatus() {
        return status;
    }

    public void setStatus(VoucherStatus status) {
        this.status = status;
    }

    public boolean isSingleUse() {
        return singleUse;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(Instant disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public boolean isForceDisconnected() {
        return forceDisconnected;
    }

    public void setForceDisconnected(boolean forceDisconnected) {
        this.forceDisconnected = forceDisconnected;
    }
}
