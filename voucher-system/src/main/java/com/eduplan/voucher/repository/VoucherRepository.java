package com.eduplan.voucher.repository;

import com.eduplan.voucher.model.Voucher;
import com.eduplan.voucher.model.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    List<Voucher> findByStatus(VoucherStatus status);

    List<Voucher> findByStatusAndExpiresAtBefore(VoucherStatus status, Instant instant);

    List<Voucher> findAllByOrderByCreatedAtDesc();
}
