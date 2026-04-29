package com.journal.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.journal.entity.enums.ReviewResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reminderSentAt;
    private ReviewResult result;
    private String comment;
    private String confidentialComment;
    private int recommendationScore;
    private boolean conflictOfInterest;
    private String conflictReason;
    private boolean active;
    private String status;
}
