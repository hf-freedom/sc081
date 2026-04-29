package com.journal.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationRecord {
    private Long id;
    private Long submissionId;
    private String doi;
    private String volume;
    private String issue;
    private int startPage;
    private int endPage;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;
    private String pdfUrl;
    private String xmlUrl;
    private String htmlUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
