package com.journal.entity.enums;

public enum ReviewResult {
    ACCEPT("录用"),
    MINOR_REVISION("小修"),
    MAJOR_REVISION("大修"),
    REJECT("拒稿");

    private final String description;

    ReviewResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
