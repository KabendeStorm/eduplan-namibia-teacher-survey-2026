package com.eduplan.voucher.service;

import com.eduplan.voucher.exception.InvalidVoucherStateException;
import com.eduplan.voucher.exception.VoucherNotFoundException;
import com.eduplan.voucher.model.Voucher;
import com.eduplan.voucher.model.VoucherDuration;
import com.eduplan.voucher.model.VoucherStatus;
import com.eduplan.voucher.repository.VoucherRepository;
import com.eduplan.voucher.util.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class VoucherService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public Voucher generateVoucher(VoucherDuration duration, boolean singleUse) {
        String code = generateUniqueCode();
        Voucher voucher = new Voucher(code, duration, singleUse);
        return voucherRepository.save(voucher);
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = CodeGenerator.generate();
            if (!voucherRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique voucher code after "
                + MAX_CODE_GENERATION_ATTEMPTS + " attempts");
    }

    @Transactional(readOnly = true)
    public List<Voucher> listAll() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Voucher> listActive() {
        return voucherRepository.findByStatus(VoucherStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Voucher getByCode(String code) {
        return voucherRepository.findByCode(normalize(code))
                .orElseThrow(() -> new VoucherNotFoundException(code));
    }

    /**
     * Simulates a client connecting to the network with the given voucher code.
     * Starts the timed session: status becomes ACTIVE and the expiry timestamp is
     * fixed at "now + voucher duration".
     */
    @Transactional
    public Voucher connect(String code, String clientIdentifier) {
        Voucher voucher = voucherRepository.findByCode(normalize(code))
                .orElseThrow(() -> new VoucherNotFoundException(code));

        if (voucher.getStatus() == VoucherStatus.ACTIVE) {
            throw new InvalidVoucherStateException(
                    "Voucher '" + voucher.getCode() + "' is already connected on another client");
        }
        if (voucher.getStatus() == VoucherStatus.EXPIRED) {
            throw new InvalidVoucherStateException(
                    "Voucher '" + voucher.getCode() + "' has already been used or has expired");
        }

        Instant now = Instant.now();
        voucher.setConnectedAt(now);
        voucher.setExpiresAt(now.plus(voucher.getDuration().toDuration()));
        voucher.setClientIdentifier(clientIdentifier);
        voucher.setStatus(VoucherStatus.ACTIVE);
        return voucher;
    }

    /**
     * Force-disconnects an active session (admin action from the dashboard), or
     * simulates the natural end of session when invoked by the expiry scheduler.
     */
    @Transactional
    public Voucher disconnect(String code, boolean manual) {
        Voucher voucher = voucherRepository.findByCode(normalize(code))
                .orElseThrow(() -> new VoucherNotFoundException(code));

        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new InvalidVoucherStateException(
                    "Voucher '" + voucher.getCode() + "' is not currently connected");
        }

        voucher.setStatus(VoucherStatus.EXPIRED);
        voucher.setDisconnectedAt(Instant.now());
        voucher.setForceDisconnected(manual);
        return voucher;
    }

    /**
     * Scans for ACTIVE vouchers whose granted duration has elapsed and expires
     * them, simulating the automatic network disconnection.
     */
    @Transactional
    public List<Voucher> expireDueSessions() {
        List<Voucher> due = voucherRepository.findByStatusAndExpiresAtBefore(
                VoucherStatus.ACTIVE, Instant.now());
        for (Voucher voucher : due) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucher.setDisconnectedAt(Instant.now());
            voucher.setForceDisconnected(false);
        }
        return due;
    }

    private String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }
}
