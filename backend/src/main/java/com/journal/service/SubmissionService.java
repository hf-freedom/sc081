package com.journal.service;

import com.journal.entity.*;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private ReviewerService reviewerService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PublicationFeeService feeService;

    @Autowired
    private PublicationService publicationService;

    public Submission createSubmission(Submission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("投稿信息不能为空");
        }
        
        Author author = authorService.getAuthorById(submission.getAuthorId());
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        
        if (author.isBlacklisted()) {
            throw new IllegalArgumentException("该作者在黑名单中，无法投稿");
        }
        
        Section section = journalService.getSectionById(submission.getSectionId());
        if (section == null || !section.isActive()) {
            throw new IllegalArgumentException("所选栏目无效或已停用");
        }
        
        if (submission.getPageCount() < section.getMinPages() || submission.getPageCount() > section.getMaxPages()) {
            throw new IllegalArgumentException("页数不符合栏目要求（" + section.getMinPages() + "-" + section.getMaxPages() + "页）");
        }
        
        boolean hasDuplicate = dataStore.getSubmissions().values().stream()
                .anyMatch(s -> s.getAuthorId().equals(submission.getAuthorId())
                        && s.getTitle().equalsIgnoreCase(submission.getTitle())
                        && (s.getStatus() == SubmissionStatus.SUBMITTED
                        || s.getStatus() == SubmissionStatus.INITIAL_REVIEW
                        || s.getStatus() == SubmissionStatus.REVIEWING
                        || s.getStatus() == SubmissionStatus.REVISION_NEEDED));
        if (hasDuplicate) {
            throw new IllegalArgumentException("存在相同标题的待处理投稿");
        }
        
        Journal journal = journalService.getMainJournal();
        submission.setId(dataStore.getNextSubmissionId());
        submission.setJournalId(journal.getId());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        
        if (submission.getCoAuthorIds() == null) {
            submission.setCoAuthorIds(new ArrayList<>());
        }
        if (submission.getReviewIds() == null) {
            submission.setReviewIds(new ArrayList<>());
        }
        if (submission.getRevisionIds() == null) {
            submission.setRevisionIds(new ArrayList<>());
        }
        
        dataStore.getSubmissions().put(submission.getId(), submission);
        authorService.addSubmissionToAuthor(author.getId(), submission.getId());
        
        return submission;
    }

    public Submission getSubmissionById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("投稿ID不能为空");
        }
        return dataStore.getSubmissions().get(id);
    }

    public List<Submission> getAllSubmissions() {
        return new ArrayList<>(dataStore.getSubmissions().values());
    }

    public List<Submission> getSubmissionsByStatus(SubmissionStatus status) {
        return dataStore.getSubmissions().values().stream()
                .filter(s -> s.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Submission startInitialReview(Long submissionId) {
        Submission submission = getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        if (submission.getStatus() != SubmissionStatus.SUBMITTED) {
            throw new IllegalArgumentException("只有已投稿状态的稿件可以开始初审");
        }
        
        submission.setStatus(SubmissionStatus.INITIAL_REVIEW);
        submission.setInitialReviewStartedAt(LocalDateTime.now());
        return submission;
    }

    public Submission completeInitialReview(Long submissionId, boolean passed, String comment) {
        Submission submission = getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        if (submission.getStatus() != SubmissionStatus.INITIAL_REVIEW) {
            throw new IllegalArgumentException("只有初审中的稿件可以完成初审");
        }
        
        submission.setInitialReviewCompletedAt(LocalDateTime.now());
        submission.setInitialReviewComment(comment);
        
        if (passed) {
            Journal journal = journalService.getMainJournal();
            if (journal == null) {
                throw new IllegalArgumentException("期刊不存在，无法分配审稿人");
            }
            if (journal.getReviewRule() == null) {
                throw new IllegalArgumentException("审稿规则未配置");
            }
            
            Author author = authorService.getAuthorById(submission.getAuthorId());
            if (author == null) {
                throw new IllegalArgumentException("作者不存在");
            }
            
            List<Reviewer> eligibleReviewers = reviewerService.findEligibleReviewers(
                    author.getInstitution(), null);
            
            int needed = journal.getReviewRule().getReviewerCount();
            if (eligibleReviewers.size() < needed) {
                throw new IllegalArgumentException("可用审稿人不足，需要" + needed + "人，但只有" + eligibleReviewers.size() + "人可用");
            }
            
            submission.setStatus(SubmissionStatus.REVIEWING);
            submission.setReviewingStartedAt(LocalDateTime.now());
            
            for (int i = 0; i < needed; i++) {
                Reviewer reviewer = eligibleReviewers.get(i);
                Review review = reviewService.createReview(submissionId, reviewer.getId(), journal.getReviewRule().getReviewDays());
                submission.getReviewIds().add(review.getId());
            }
        } else {
            submission.setStatus(SubmissionStatus.INITIAL_REVIEW_FAILED);
        }
        
        return submission;
    }

    public Submission withdraw(Long submissionId, String reason) {
        Submission submission = getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        if (submission.getStatus() == SubmissionStatus.PUBLISHED) {
            throw new IllegalArgumentException("已出版的稿件无法撤稿");
        }
        
        SubmissionStatus currentStatus = submission.getStatus();
        BigDecimal withdrawFee = BigDecimal.ZERO;
        
        if (currentStatus == SubmissionStatus.REVIEWING || currentStatus == SubmissionStatus.REVISION_NEEDED) {
            withdrawFee = new BigDecimal("500");
        } else if (currentStatus == SubmissionStatus.ACCEPTED || currentStatus == SubmissionStatus.FEE_PENDING || 
                   currentStatus == SubmissionStatus.FEE_PAID || currentStatus == SubmissionStatus.SCHEDULED) {
            withdrawFee = new BigDecimal("1000");
        }
        
        submission.setStatus(SubmissionStatus.WITHDRAWN);
        submission.setWithdrawnAt(LocalDateTime.now());
        submission.setWithdrawReason(reason);
        submission.setWithdrawFee(withdrawFee);
        
        return submission;
    }

    public List<Submission> getSubmissionsByAuthorId(Long authorId) {
        Author author = authorService.getAuthorById(authorId);
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        return author.getSubmissionIds().stream()
                .map(dataStore.getSubmissions()::get)
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }
}
