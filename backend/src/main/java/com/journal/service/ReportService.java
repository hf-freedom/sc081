package com.journal.service;

import com.journal.entity.PublicationFee;
import com.journal.entity.Report;
import com.journal.entity.Review;
import com.journal.entity.Submission;
import com.journal.entity.enums.PaymentStatus;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private InMemoryDataStore dataStore;

    public Report generateMonthlyReport(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = month == 12 
                ? LocalDateTime.of(year + 1, 1, 1, 0, 0)
                : LocalDateTime.of(year, month + 1, 1, 0, 0);
        
        List<Submission> submissionsInPeriod = dataStore.getSubmissions().values().stream()
                .filter(s -> s.getSubmittedAt() != null && 
                        !s.getSubmittedAt().isBefore(startDate) && 
                        s.getSubmittedAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        int totalSubmissions = submissionsInPeriod.size();
        int initialReviewPassed = (int) submissionsInPeriod.stream()
                .filter(s -> s.getInitialReviewCompletedAt() != null && 
                        s.getStatus() != SubmissionStatus.INITIAL_REVIEW_FAILED)
                .count();
        int initialReviewFailed = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.INITIAL_REVIEW_FAILED)
                .count();
        int inReview = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.REVIEWING)
                .count();
        int revisionNeeded = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.REVISION_NEEDED)
                .count();
        int accepted = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED ||
                        s.getStatus() == SubmissionStatus.FEE_PENDING ||
                        s.getStatus() == SubmissionStatus.FEE_PAID ||
                        s.getStatus() == SubmissionStatus.SCHEDULED ||
                        s.getStatus() == SubmissionStatus.PUBLISHED)
                .count();
        int rejected = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.REJECTED)
                .count();
        int published = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.PUBLISHED)
                .count();
        int withdrawn = (int) submissionsInPeriod.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.WITHDRAWN)
                .count();
        
        BigDecimal acceptanceRate = totalSubmissions > 0 
                ? BigDecimal.valueOf((double) accepted / totalSubmissions * 100)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        List<Review> completedReviews = dataStore.getReviews().values().stream()
                .filter(r -> r.getCompletedAt() != null &&
                        !r.getCompletedAt().isBefore(startDate) &&
                        r.getCompletedAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        double totalReviewDays = completedReviews.stream()
                .filter(r -> r.getAssignedAt() != null && r.getCompletedAt() != null)
                .mapToDouble(r -> ChronoUnit.DAYS.between(r.getAssignedAt(), r.getCompletedAt()))
                .sum();
        BigDecimal averageReviewDays = completedReviews.size() > 0
                ? BigDecimal.valueOf(totalReviewDays / completedReviews.size())
                        .setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        List<PublicationFee> paidFees = dataStore.getFees().values().stream()
                .filter(f -> f.getStatus() == PaymentStatus.PAID &&
                        f.getPaidAt() != null &&
                        !f.getPaidAt().isBefore(startDate) &&
                        f.getPaidAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        BigDecimal totalFeesCollected = paidFees.stream()
                .map(f -> f.getPaidAmount() != null ? f.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int feesCollectedCount = paidFees.size();
        
        int overdueReviews = (int) dataStore.getReviews().values().stream()
                .filter(r -> r.isActive() && !"COMPLETED".equals(r.getStatus()))
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        
        Report report = Report.builder()
                .id(dataStore.getNextReportId())
                .period(year + "-" + String.format("%02d", month))
                .year(year)
                .month(month)
                .totalSubmissions(totalSubmissions)
                .initialReviewPassed(initialReviewPassed)
                .initialReviewFailed(initialReviewFailed)
                .inReview(inReview)
                .revisionNeeded(revisionNeeded)
                .accepted(accepted)
                .rejected(rejected)
                .published(published)
                .withdrawn(withdrawn)
                .acceptanceRate(acceptanceRate)
                .averageReviewDays(averageReviewDays)
                .averageReviewerCount(2)
                .totalFeesCollected(totalFeesCollected)
                .feesCollectedCount(feesCollectedCount)
                .overdueReviews(overdueReviews)
                .generatedAt(LocalDateTime.now())
                .build();
        
        dataStore.getReports().put(report.getId(), report);
        return report;
    }

    public Report getReportById(Long id) {
        return dataStore.getReports().get(id);
    }

    public List<Report> getReportsByYear(int year) {
        return dataStore.getReports().values().stream()
                .filter(r -> r.getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Report> getAllReports() {
        return new ArrayList<>(dataStore.getReports().values());
    }
}
