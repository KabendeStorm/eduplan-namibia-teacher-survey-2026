package com.eduplan.voucher.model;

import java.time.Duration;

/**
 * Fixed set of access durations that a voucher can grant.
 */
public enum VoucherDuration {

    TEN_MIN(10, "10 Minutes"),
    TWENTY_MIN(20, "20 Minutes"),
    THIRTY_MIN(30, "30 Minutes"),
    ONE_HOUR(60, "1 Hour"),
    ONE_HOUR_THIRTY_MIN(90, "1 Hour 30 Minutes"),
    TWO_HOUR(120, "2 Hours"),
    THREE_HOUR(180, "3 Hours"),
    FIVE_HOUR(300, "5 Hours");

    private final int minutes;
    private final String label;

    VoucherDuration(int minutes, String label) {
        this.minutes = minutes;
        this.label = label;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getLabel() {
        return label;
    }

    public Duration toDuration() {
        return Duration.ofMinutes(minutes);
    }
}
