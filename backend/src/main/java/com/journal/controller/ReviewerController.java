package com.journal.controller;

import com.journal.entity.Review;
import com.journal.entity.Reviewer;
import com.journal.service.ReviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/reviewers")
@CrossOrigin(origins = "http://localhost:3002")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    @GetMapping
    public ResponseEntity<List<Reviewer>> getAllReviewers() {
        List<Reviewer> reviewers = reviewerService.getAllReviewers();
        return ResponseEntity.ok(reviewers);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Reviewer>> getActiveReviewers() {
        List<Reviewer> reviewers = reviewerService.getActiveReviewers();
        return ResponseEntity.ok(reviewers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reviewer> getReviewerById(@PathVariable Long id) {
        Reviewer reviewer = reviewerService.getReviewerById(id);
        if (reviewer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reviewer);
    }

    @PostMapping
    public ResponseEntity<?> createReviewer(@RequestBody Reviewer reviewer) {
        try {
            Reviewer created = reviewerService.createReviewer(reviewer);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReviewer(@PathVariable Long id, @RequestBody Reviewer reviewer) {
        try {
            Reviewer updated = reviewerService.updateReviewer(id, reviewer);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<?> setActive(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        try {
            boolean active = params.getOrDefault("active", true);
            reviewerService.setActive(id, active);
            return ResponseEntity.ok(MapBuilder.of("message", "操作成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviewerReviews(@PathVariable Long id) {
        try {
            List<Review> reviews = reviewerService.getReviewerReviews(id);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/eligible")
    public ResponseEntity<List<Reviewer>> findEligibleReviewers(@RequestBody Map<String, String> params) {
        String authorInstitution = params.get("authorInstitution");
        String researchField = params.get("researchField");
        List<Reviewer> reviewers = reviewerService.findEligibleReviewers(authorInstitution, researchField);
        return ResponseEntity.ok(reviewers);
    }
}
