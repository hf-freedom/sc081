package com.journal.controller;

import com.journal.entity.Review;
import com.journal.entity.enums.ReviewResult;
import com.journal.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3002")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<Review>> getReviewsBySubmissionId(@PathVariable Long submissionId) {
        List<Review> reviews = reviewService.getReviewsBySubmissionId(submissionId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<Review>> getReviewsByReviewerId(@PathVariable Long reviewerId) {
        List<Review> reviews = reviewService.getReviewsByReviewerId(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeReview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String resultStr = (String) params.get("result");
            ReviewResult result = ReviewResult.valueOf(resultStr);
            String comment = (String) params.get("comment");
            String confidentialComment = (String) params.get("confidentialComment");
            Integer recommendationScore = (Integer) params.getOrDefault("recommendationScore", 0);
            
            Review updated = reviewService.completeReview(id, result, comment, confidentialComment, recommendationScore);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Review>> getOverdueReviews() {
        List<Review> reviews = reviewService.getOverdueReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}/replace-reviewer")
    public ResponseEntity<?> replaceReviewer(@PathVariable Long id, @RequestBody Map<String, Long> params) {
        try {
            Long newReviewerId = params.get("newReviewerId");
            Review updated = reviewService.replaceReviewer(id, newReviewerId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
