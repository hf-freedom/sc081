package com.journal.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private String period;
    private int year;
    private int month;
    private int totalSubmissions;
    private int initialReviewPassed;
    private int initialReviewFailed;
    private int inReview;
    private int revisionNeeded;
    private int accepted;
    private int rejected;
    private int published;
    private int withdrawn;
    private BigDecimal acceptanceRate;
    private BigDecimal averageReviewDays;
    private int averageReviewerCount;
    private BigDecimal totalFeesCollected;
    private int feesCollectedCount;
    private int overdueReviews;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
}
