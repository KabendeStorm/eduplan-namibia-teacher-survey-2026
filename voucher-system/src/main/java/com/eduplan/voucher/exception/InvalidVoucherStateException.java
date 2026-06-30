package com.eduplan.voucher.exception;

public class InvalidVoucherStateException extends RuntimeException {
    public InvalidVoucherStateException(String message) {
        super(message);
    }
}
