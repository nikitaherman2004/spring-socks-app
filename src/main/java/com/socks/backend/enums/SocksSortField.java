package com.socks.backend.enums;

public enum SocksSortField {

    COLOR("color"),

    COTTON_PERCENTAGE("cottonPercentage");

    private final String value;

    SocksSortField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
