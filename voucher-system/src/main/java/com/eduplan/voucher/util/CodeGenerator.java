package com.eduplan.voucher.util;

import java.security.SecureRandom;

/**
 * Generates random voucher codes from a charset that excludes visually
 * ambiguous characters (0/O, 1/I/L) so codes are easy to read and type
 * at a captive-portal screen.
 */
public final class CodeGenerator {

    private static final String CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int SEGMENT_LENGTH = 4;
    private static final int SEGMENT_COUNT = 2;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    /** Produces a code shaped like "AB12-CD34". */
    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int segment = 0; segment < SEGMENT_COUNT; segment++) {
            if (segment > 0) {
                sb.append('-');
            }
            for (int i = 0; i < SEGMENT_LENGTH; i++) {
                sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
            }
        }
        return sb.toString();
    }
}
