package com.english12smart.constant;

public enum ClassroomStatus {

    ACTIVE,
    UPCOMING,
    COMPLETED;

    public static ClassroomStatus from(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ClassroomStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
