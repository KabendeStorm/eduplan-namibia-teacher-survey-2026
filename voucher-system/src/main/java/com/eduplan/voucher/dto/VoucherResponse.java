package com.eduplan.voucher.dto;

import com.eduplan.voucher.model.Voucher;
import com.eduplan.voucher.model.VoucherStatus;

import java.time.Instant;

public class VoucherResponse {

    private final String code;
    private final String duration;
    private final int durationMinutes;
    private final VoucherStatus status;
    private final boolean singleUse;
    private final Instant createdAt;
    private final Instant connectedAt;
    private final Instant expiresAt;
    private final String clientIdentifier;
    private final boolean forceDisconnected;
    private final Long secondsRemaining;

    public VoucherResponse(Voucher v) {
        this.code = v.getCode();
        this.duration = v.getDuration().getLabel();
        this.durationMinutes = v.getDuration().getMinutes();
        this.status = v.getStatus();
        this.singleUse = v.isSingleUse();
        this.createdAt = v.getCreatedAt();
        this.connectedAt = v.getConnectedAt();
        this.expiresAt = v.getExpiresAt();
        this.clientIdentifier = v.getClientIdentifier();
        this.forceDisconnected = v.isForceDisconnected();

        if (v.getStatus() == VoucherStatus.ACTIVE && v.getExpiresAt() != null) {
            long remaining = v.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
            this.secondsRemaining = Math.max(remaining, 0);
        } else {
            this.secondsRemaining = null;
        }
    }

    public String getCode() {
        return code;
    }

    public String getDuration() {
        return duration;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public VoucherStatus getStatus() {
        return status;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public boolean isForceDisconnected() {
        return forceDisconnected;
    }

    public Long getSecondsRemaining() {
        return secondsRemaining;
    }
}
