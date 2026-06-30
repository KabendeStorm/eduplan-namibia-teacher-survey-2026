package com.eduplan.voucher.service;

import com.eduplan.voucher.model.Voucher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically sweeps active voucher sessions and expires (simulated
 * disconnects) any whose granted duration has elapsed.
 */
@Component
public class VoucherExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(VoucherExpiryScheduler.class);

    private final VoucherService voucherService;

    public VoucherExpiryScheduler(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @Scheduled(fixedRateString = "${voucher.expiry-scan-interval-ms:5000}")
    public void expireDueSessions() {
        List<Voucher> expired = voucherService.expireDueSessions();
        if (!expired.isEmpty()) {
            expired.forEach(v -> log.info("Voucher '{}' session expired; client disconnected", v.getCode()));
        }
    }
}
