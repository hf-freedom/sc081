package com.journal.entity.enums;

public enum SubmissionStatus {
    SUBMITTED("已投稿"),
    INITIAL_REVIEW("初审中"),
    INITIAL_REVIEW_FAILED("初审未通过"),
    REVIEWING("审稿中"),
    REVISION_NEEDED("需修改"),
    REJECTED("拒稿"),
    ACCEPTED("已录用"),
    FEE_PENDING("待缴费"),
    FEE_PAID("已缴费"),
    SCHEDULED("排期中"),
    PUBLISHED("已出版"),
    WITHDRAWN("已撤稿");

    private final String description;

    SubmissionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
