package com.journal.service;

import com.journal.entity.*;
import com.journal.entity.enums.ReviewResult;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private ReviewerService reviewerService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private PublicationFeeService feeService;

    public Review createReview(Long submissionId, Long reviewerId, int reviewDays) {
        Submission submission = submissionService.getSubmissionById(submissionId);
        Reviewer reviewer = reviewerService.getReviewerById(reviewerId);
        
        if (submission == null || reviewer == null) {
            throw new IllegalArgumentException("投稿或审稿人不存在");
        }
        
        Review review = Review.builder()
                .id(dataStore.getNextReviewId())
                .submissionId(submissionId)
                .reviewerId(reviewerId)
                .assignedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(reviewDays))
                .active(true)
                .status("ASSIGNED")
                .build();
        
        dataStore.getReviews().put(review.getId(), review);
        reviewerService.addReviewToReviewer(reviewerId, review.getId());
        
        return review;
    }

    public Review getReviewById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审稿ID不能为空");
        }
        return dataStore.getReviews().get(id);
    }

    public List<Review> getReviewsBySubmissionId(Long submissionId) {
        return dataStore.getReviews().values().stream()
                .filter(r -> r.getSubmissionId().equals(submissionId))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsByReviewerId(Long reviewerId) {
        return dataStore.getReviews().values().stream()
                .filter(r -> r.getReviewerId().equals(reviewerId))
                .collect(Collectors.toList());
    }

    public Review completeReview(Long reviewId, ReviewResult result, String comment, 
                                  String confidentialComment, int recommendationScore) {
        Review review = getReviewById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("审稿不存在");
        }
        if (!review.isActive()) {
            throw new IllegalArgumentException("该审稿已失效");
        }
        if ("COMPLETED".equals(review.getStatus())) {
            throw new IllegalArgumentException("该审稿已完成");
        }
        
        review.setResult(result);
        review.setComment(comment);
        review.setConfidentialComment(confidentialComment);
        review.setRecommendationScore(recommendationScore);
        review.setCompletedAt(LocalDateTime.now());
        review.setStatus("COMPLETED");
        
        checkSubmissionReviews(review.getSubmissionId());
        
        return review;
    }

    public void checkSubmissionReviews(Long submissionId) {
        List<Review> reviews = getReviewsBySubmissionId(submissionId);
        Submission submission = submissionService.getSubmissionById(submissionId);
        
        if (submission == null) return;
        
        long activeReviews = reviews.stream().filter(Review::isActive).count();
        long completedReviews = reviews.stream()
                .filter(r -> r.isActive() && "COMPLETED".equals(r.getStatus()))
                .count();
        
        if (activeReviews == completedReviews && activeReviews > 0) {
            aggregateReviewResults(submission, reviews);
        }
    }

    private void aggregateReviewResults(Submission submission, List<Review> reviews) {
        long acceptCount = reviews.stream().filter(r -> r.getResult() == ReviewResult.ACCEPT).count();
        long minorRevisionCount = reviews.stream().filter(r -> r.getResult() == ReviewResult.MINOR_REVISION).count();
        long majorRevisionCount = reviews.stream().filter(r -> r.getResult() == ReviewResult.MAJOR_REVISION).count();
        long rejectCount = reviews.stream().filter(r -> r.getResult() == ReviewResult.REJECT).count();
        
        if (rejectCount >= 1) {
            submission.setStatus(SubmissionStatus.REJECTED);
            submission.setReviewingCompletedAt(LocalDateTime.now());
        } else if (majorRevisionCount >= 1) {
            submission.setStatus(SubmissionStatus.REVISION_NEEDED);
            submission.setReviewingCompletedAt(LocalDateTime.now());
            createRevision(submission.getId(), false, reviews);
        } else if (minorRevisionCount >= 1) {
            submission.setStatus(SubmissionStatus.REVISION_NEEDED);
            submission.setReviewingCompletedAt(LocalDateTime.now());
            createRevision(submission.getId(), true, reviews);
        } else if (acceptCount == reviews.size()) {
            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setAcceptedAt(LocalDateTime.now());
            submission.setReviewingCompletedAt(LocalDateTime.now());
            feeService.createFee(submission.getId());
        }
    }

    private Revision createRevision(Long submissionId, boolean isMinor, List<Review> reviews) {
        StringBuilder instructions = new StringBuilder();
        for (Review review : reviews) {
            if (review.getComment() != null) {
                instructions.append("审稿人 ").append(review.getReviewerId()).append(" 意见：\n");
                instructions.append(review.getComment()).append("\n\n");
            }
        }
        
        int revisionDays = isMinor ? 14 : 30;
        
        Revision revision = Revision.builder()
                .id(dataStore.getNextRevisionId())
                .submissionId(submissionId)
                .revisionNumber(1)
                .requestedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(revisionDays))
                .instructions(instructions.toString())
                .isMinor(isMinor)
                .status("REQUESTED")
                .build();
        
        dataStore.getRevisions().put(revision.getId(), revision);
        
        Submission submission = submissionService.getSubmissionById(submissionId);
        if (submission != null) {
            submission.getRevisionIds().add(revision.getId());
        }
        
        return revision;
    }

    public List<Review> getOverdueReviews() {
        LocalDateTime now = LocalDateTime.now();
        return dataStore.getReviews().values().stream()
                .filter(r -> r.isActive() && !"COMPLETED".equals(r.getStatus()))
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(now))
                .collect(Collectors.toList());
    }

    public List<Review> getReviewsNeedReminder() {
        Journal journal = journalService.getMainJournal();
        if (journal == null || journal.getReviewRule() == null) {
            return java.util.Collections.emptyList();
        }
        
        int reminderDays = journal.getReviewRule().getReminderDaysBeforeDeadline();
        LocalDateTime reminderThreshold = LocalDateTime.now().plusDays(reminderDays);
        
        return dataStore.getReviews().values().stream()
                .filter(r -> r.isActive() && !"COMPLETED".equals(r.getStatus()))
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(reminderThreshold))
                .filter(r -> r.getReminderSentAt() == null)
                .collect(Collectors.toList());
    }

    public void markReminderSent(Long reviewId) {
        Review review = getReviewById(reviewId);
        if (review != null) {
            review.setReminderSentAt(LocalDateTime.now());
        }
    }

    public Review replaceReviewer(Long reviewId, Long newReviewerId) {
        Review oldReview = getReviewById(reviewId);
        if (oldReview == null) {
            throw new IllegalArgumentException("审稿不存在");
        }
        
        oldReview.setActive(false);
        oldReview.setConflictOfInterest(true);
        oldReview.setConflictReason("审稿人更换");
        
        Journal journal = journalService.getMainJournal();
        Review newReview = createReview(oldReview.getSubmissionId(), newReviewerId, 
                journal.getReviewRule().getReviewDays());
        
        return newReview;
    }
}
