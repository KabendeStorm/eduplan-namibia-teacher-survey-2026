package com.eduplan.voucher.model;

/**
 * Lifecycle states of a voucher.
 */
public enum VoucherStatus {
    /** Generated but never redeemed/connected. */
    UNUSED,
    /** Redeemed and currently within its granted duration. */
    ACTIVE,
    /** Duration elapsed, or it was force-disconnected, or it was redeemed already (single-use). */
    EXPIRED
}
