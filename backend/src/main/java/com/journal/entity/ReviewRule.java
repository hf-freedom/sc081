package com.journal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRule {
    private int reviewerCount;
    private int reviewDays;
    private boolean doubleBlind;
    private boolean allowConflictReject;
    private int reminderDaysBeforeDeadline;
}
