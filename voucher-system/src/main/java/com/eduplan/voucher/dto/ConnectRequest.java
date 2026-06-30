package com.eduplan.voucher.dto;

public class ConnectRequest {

    /** Optional simulated client identifier, e.g. device name or MAC address. */
    private String clientIdentifier;

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }
}
