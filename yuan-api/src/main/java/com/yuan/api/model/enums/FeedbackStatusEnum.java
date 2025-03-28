package com.yuan.api.model.enums;

public enum FeedbackStatusEnum {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    IGNORED("Ignored");

    private final String status;

    FeedbackStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return this.status;
    }

    public static FeedbackStatusEnum valueOfStatus(String status) {
        for (FeedbackStatusEnum e : values()) {
            if (e.getStatus().equals(status)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant with status: " + status);
    }
}

