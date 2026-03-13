package com.ra.batshop.model.Enum;

public enum ContactStatus {
    UNREAD("Chưa đọc"),
    PROCESSING("Đang xử lý"),
    RESOLVED("Đã giải quyết"),
    REJECTED("Từ chối/Thư rác"); // Trạng thái mới

    private final String label;

    ContactStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}