package com.eduplan.voucher.service;

import com.eduplan.voucher.exception.InvalidVoucherStateException;
import com.eduplan.voucher.exception.VoucherNotFoundException;
import com.eduplan.voucher.model.Voucher;
import com.eduplan.voucher.model.VoucherDuration;
import com.eduplan.voucher.model.VoucherStatus;
import com.eduplan.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext
class VoucherServiceTest {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Test
    void generatedVoucherStartsAsUnusedWithExpectedDuration() {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.THIRTY_MIN, true);

        assertThat(voucher.getCode()).isNotBlank();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.UNUSED);
        assertThat(voucher.getDuration()).isEqualTo(VoucherDuration.THIRTY_MIN);
        assertThat(voucher.isSingleUse()).isTrue();
        assertThat(voucher.getConnectedAt()).isNull();
        assertThat(voucher.getExpiresAt()).isNull();
    }

    @Test
    void generatedCodesAreUnique() {
        Set<String> codes = new HashSet<>();
        IntStream.range(0, 50).forEach(i -> {
            Voucher v = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);
            codes.add(v.getCode());
        });
        assertThat(codes).hasSize(50);
    }

    @Test
    void connectingActivatesVoucherAndSetsExpiry() {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);

        Voucher connected = voucherService.connect(voucher.getCode(), "device-1");

        assertThat(connected.getStatus()).isEqualTo(VoucherStatus.ACTIVE);
        assertThat(connected.getConnectedAt()).isNotNull();
        assertThat(connected.getExpiresAt()).isAfter(connected.getConnectedAt());
        assertThat(connected.getClientIdentifier()).isEqualTo("device-1");

        long grantedSeconds = connected.getExpiresAt().getEpochSecond() - connected.getConnectedAt().getEpochSecond();
        assertThat(grantedSeconds).isEqualTo(VoucherDuration.TEN_MIN.toDuration().toSeconds());
    }

    @Test
    void connectingTwiceWithSingleUseVoucherFails() {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);
        voucherService.connect(voucher.getCode(), "device-1");

        assertThatThrownBy(() -> voucherService.connect(voucher.getCode(), "device-2"))
                .isInstanceOf(InvalidVoucherStateException.class);
    }

    @Test
    void connectingUnknownCodeThrowsNotFound() {
        assertThatThrownBy(() -> voucherService.connect("ZZZZ-ZZZZ", null))
                .isInstanceOf(VoucherNotFoundException.class);
    }

    @Test
    void forceDisconnectExpiresActiveVoucher() {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);
        voucherService.connect(voucher.getCode(), "device-1");

        Voucher disconnected = voucherService.disconnect(voucher.getCode(), true);

        assertThat(disconnected.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
        assertThat(disconnected.isForceDisconnected()).isTrue();
        assertThat(disconnected.getDisconnectedAt()).isNotNull();
    }

    @Test
    void disconnectingNonActiveVoucherFails() {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);

        assertThatThrownBy(() -> voucherService.disconnect(voucher.getCode(), true))
                .isInstanceOf(InvalidVoucherStateException.class);
    }

    @Test
    void scheduledSweepExpiresSessionsPastTheirDuration() throws Exception {
        Voucher voucher = voucherService.generateVoucher(VoucherDuration.TEN_MIN, true);
        Voucher connected = voucherService.connect(voucher.getCode(), "device-1");

        // backdate the expiry so the sweep treats it as elapsed, instead of sleeping in the test
        backdateExpiry(connected, Instant.now().minusSeconds(5));
        voucherRepository.saveAndFlush(connected);

        List<Voucher> expired = voucherService.expireDueSessions();

        assertThat(expired).extracting(Voucher::getCode).contains(voucher.getCode());

        Voucher reloaded = voucherService.getByCode(voucher.getCode());
        assertThat(reloaded.getStatus()).isEqualTo(VoucherStatus.EXPIRED);
        assertThat(reloaded.isForceDisconnected()).isFalse();
    }

    private void backdateExpiry(Voucher voucher, Instant expiresAt) throws Exception {
        Field field = Voucher.class.getDeclaredField("expiresAt");
        field.setAccessible(true);
        field.set(voucher, expiresAt);
    }
}
