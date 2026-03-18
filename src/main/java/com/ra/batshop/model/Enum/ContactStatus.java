package com.ra.batshop.model.Enum;

public enum ContactStatus {
    UNREAD("Chưa đọc"),
    PROCESSING("Đang xử lý"),
    RESOLVED("Đã phản hồi"),
    REJECTED("Đã bỏ qua/Thư rác");

    private final String label;

    ContactStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}