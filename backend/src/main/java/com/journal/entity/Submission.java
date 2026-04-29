package com.journal.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.journal.entity.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    private Long id;
    private String title;
    private String abstracts;
    private String keywords;
    private Long authorId;
    private List<Long> coAuthorIds = new ArrayList<>();
    private Long sectionId;
    private Long journalId;
    private int pageCount;
    private boolean colorPrint;
    private boolean openAccess;
    private String manuscriptFile;
    private String supplementaryFiles;
    private SubmissionStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime initialReviewStartedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime initialReviewCompletedAt;
    private String initialReviewComment;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewingStartedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewingCompletedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime withdrawnAt;
    private String withdrawReason;
    private BigDecimal withdrawFee;
    
    private List<Long> reviewIds = new ArrayList<>();
    private List<Long> revisionIds = new ArrayList<>();
    private Long feeId;
    private Long publicationRecordId;
}
