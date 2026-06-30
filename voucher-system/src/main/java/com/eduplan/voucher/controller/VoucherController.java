package com.eduplan.voucher.controller;

import com.eduplan.voucher.dto.ConnectRequest;
import com.eduplan.voucher.dto.DashboardSlotResponse;
import com.eduplan.voucher.dto.VoucherResponse;
import com.eduplan.voucher.model.Voucher;
import com.eduplan.voucher.model.VoucherDuration;
import com.eduplan.voucher.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/api/durations")
    public List<Map<String, Object>> listDurations() {
        return Arrays.stream(VoucherDuration.values())
                .map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("key", d.name());
                    m.put("label", d.getLabel());
                    m.put("minutes", d.getMinutes());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/api/vouchers")
    public List<VoucherResponse> listAll() {
        return voucherService.listAll().stream().map(VoucherResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/api/vouchers/active")
    public List<VoucherResponse> listActive() {
        return voucherService.listActive().stream().map(VoucherResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/api/vouchers/dashboard")
    public List<DashboardSlotResponse> dashboard() {
        Map<VoucherDuration, List<VoucherResponse>> byDuration = voucherService.listAll().stream()
                .collect(Collectors.groupingBy(
                        Voucher::getDuration,
                        LinkedHashMap::new,
                        Collectors.mapping(VoucherResponse::new, Collectors.toList())));

        return Arrays.stream(VoucherDuration.values())
                .sorted(Comparator.comparingInt(VoucherDuration::getMinutes))
                .map(d -> new DashboardSlotResponse(d, byDuration.getOrDefault(d, List.of())))
                .collect(Collectors.toList());
    }

    @PostMapping("/api/vouchers/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public VoucherResponse generate(@RequestParam VoucherDuration duration,
                                     @RequestParam(defaultValue = "true") boolean singleUse) {
        Voucher voucher = voucherService.generateVoucher(duration, singleUse);
        return new VoucherResponse(voucher);
    }

    @PostMapping("/api/vouchers/{code}/connect")
    public VoucherResponse connect(@PathVariable String code, @Valid @RequestBody(required = false) ConnectRequest request) {
        String clientId = request != null ? request.getClientIdentifier() : null;
        Voucher voucher = voucherService.connect(code, clientId);
        return new VoucherResponse(voucher);
    }

    @PostMapping("/api/vouchers/{code}/disconnect")
    public VoucherResponse disconnect(@PathVariable String code) {
        Voucher voucher = voucherService.disconnect(code, true);
        return new VoucherResponse(voucher);
    }
}
