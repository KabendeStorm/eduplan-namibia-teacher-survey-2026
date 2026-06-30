package com.eduplan.voucher.exception;

public class VoucherNotFoundException extends RuntimeException {
    public VoucherNotFoundException(String code) {
        super("No voucher found with code '" + code + "'");
    }
}
