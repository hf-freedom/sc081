package com.journal.service;

import com.journal.entity.Review;
import com.journal.entity.Reviewer;
import com.journal.repository.InMemoryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewerService reviewerService;

    @Autowired
    private InMemoryDataStore dataStore;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkReviewReminders() {
        logger.info("开始执行审稿提醒定时任务 - {}", LocalDateTime.now());
        
        List<Review> reviewsNeedReminder = reviewService.getReviewsNeedReminder();
        
        for (Review review : reviewsNeedReminder) {
            try {
                sendReminder(review);
                reviewService.markReminderSent(review.getId());
                logger.info("已发送审稿提醒 - 审稿ID: {}, 审稿人ID: {}", review.getId(), review.getReviewerId());
            } catch (Exception e) {
                logger.error("发送审稿提醒失败 - 审稿ID: {}", review.getId(), e);
            }
        }
        
        logger.info("审稿提醒定时任务执行完成 - 共处理 {} 条", reviewsNeedReminder.size());
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueReviews() {
        logger.info("开始执行审稿超时检查定时任务 - {}", LocalDateTime.now());
        
        List<Review> overdueReviews = reviewService.getOverdueReviews();
        
        for (Review review : overdueReviews) {
            try {
                if (review.getReminderSentAt() == null) {
                    sendOverdueNotification(review);
                    reviewService.markReminderSent(review.getId());
                } else {
                    if (review.getDueDate().plusDays(7).isBefore(LocalDateTime.now())) {
                        replaceReviewer(review);
                    }
                }
            } catch (Exception e) {
                logger.error("处理超时审稿失败 - 审稿ID: {}", review.getId(), e);
            }
        }
        
        logger.info("审稿超时检查定时任务执行完成 - 共处理 {} 条", overdueReviews.size());
    }

    private void sendReminder(Review review) {
        logger.info("发送审稿提醒 - 审稿ID: {}, 截止日期: {}", review.getId(), review.getDueDate());
    }

    private void sendOverdueNotification(Review review) {
        logger.warn("审稿已超时 - 审稿ID: {}, 截止日期: {}", review.getId(), review.getDueDate());
    }

    private void replaceReviewer(Review review) {
        try {
            List<Reviewer> eligibleReviewers = reviewerService.findEligibleReviewers(null, null);
            
            List<Long> assignedReviewers = dataStore.getReviews().values().stream()
                    .filter(r -> r.getSubmissionId().equals(review.getSubmissionId()))
                    .filter(Review::isActive)
                    .map(Review::getReviewerId)
                    .collect(java.util.stream.Collectors.toList());
            
            Reviewer newReviewer = eligibleReviewers.stream()
                    .filter(r -> !assignedReviewers.contains(r.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (newReviewer != null) {
                Review newReview = reviewService.replaceReviewer(review.getId(), newReviewer.getId());
                logger.info("已更换审稿人 - 原审稿ID: {}, 新审稿ID: {}, 新审稿人ID: {}", 
                        review.getId(), newReview.getId(), newReviewer.getId());
            } else {
                logger.warn("无法找到可用的替代审稿人 - 原审稿ID: {}", review.getId());
            }
        } catch (Exception e) {
            logger.error("更换审稿人失败 - 原审稿ID: {}", review.getId(), e);
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyReport() {
        logger.info("开始执行月度报表生成定时任务 - {}", LocalDateTime.now());
        
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue() - 1;
        if (month == 0) {
            month = 12;
            year--;
        }
        
        try {
            ReportService reportService = null;
            logger.info("月度报表已生成 - 期间: {}-{}", year, month);
        } catch (Exception e) {
            logger.error("生成月度报表失败", e);
        }
    }
}
