package com.eduplan.voucher.dto;

import com.eduplan.voucher.model.VoucherDuration;

import java.util.List;

public class DashboardSlotResponse {

    private final VoucherDuration duration;
    private final String label;
    private final int minutes;
    private final List<VoucherResponse> vouchers;

    public DashboardSlotResponse(VoucherDuration duration, List<VoucherResponse> vouchers) {
        this.duration = duration;
        this.label = duration.getLabel();
        this.minutes = duration.getMinutes();
        this.vouchers = vouchers;
    }

    public VoucherDuration getDuration() {
        return duration;
    }

    public String getLabel() {
        return label;
    }

    public int getMinutes() {
        return minutes;
    }

    public List<VoucherResponse> getVouchers() {
        return vouchers;
    }
}
