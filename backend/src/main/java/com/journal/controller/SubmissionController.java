package com.journal.controller;

import com.journal.entity.Submission;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "http://localhost:3002")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @GetMapping
    public ResponseEntity<List<Submission>> getAllSubmissions() {
        List<Submission> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Submission>> getSubmissionsByStatus(@PathVariable SubmissionStatus status) {
        List<Submission> submissions = submissionService.getSubmissionsByStatus(status);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Submission>> getSubmissionsByAuthorId(@PathVariable Long authorId) {
        try {
            List<Submission> submissions = submissionService.getSubmissionsByAuthorId(authorId);
            return ResponseEntity.ok(submissions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long id) {
        Submission submission = submissionService.getSubmissionById(id);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(submission);
    }

    @PostMapping
    public ResponseEntity<?> createSubmission(@RequestBody Submission submission) {
        try {
            Submission created = submissionService.createSubmission(submission);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/initial-review/start")
    public ResponseEntity<?> startInitialReview(@PathVariable Long id) {
        try {
            Submission updated = submissionService.startInitialReview(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/initial-review/complete")
    public ResponseEntity<?> completeInitialReview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean passed = (Boolean) params.getOrDefault("passed", true);
            String comment = (String) params.get("comment");
            Submission updated = submissionService.completeInitialReview(id, passed, comment);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.get("reason");
            Submission updated = submissionService.withdraw(id, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
