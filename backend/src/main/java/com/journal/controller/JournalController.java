package com.journal.controller;

import com.journal.entity.FeeStandard;
import com.journal.entity.Journal;
import com.journal.entity.ReviewRule;
import com.journal.entity.Section;
import com.journal.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "http://localhost:3002")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @GetMapping
    public ResponseEntity<Journal> getMainJournal() {
        Journal journal = journalService.getMainJournal();
        if (journal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(journal);
    }

    @GetMapping("/sections")
    public ResponseEntity<List<Section>> getAllSections() {
        List<Section> sections = journalService.getAllSections();
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/sections/active")
    public ResponseEntity<List<Section>> getActiveSections() {
        List<Section> sections = journalService.getActiveSections();
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/sections/{id}")
    public ResponseEntity<Section> getSectionById(@PathVariable Long id) {
        Section section = journalService.getSectionById(id);
        if (section == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(section);
    }

    @PostMapping("/sections")
    public ResponseEntity<?> createSection(@RequestBody Section section) {
        try {
            Section created = journalService.createSection(section);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<?> updateSection(@PathVariable Long id, @RequestBody Section section) {
        try {
            Section updated = journalService.updateSection(id, section);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/sections/{id}/active")
    public ResponseEntity<?> setSectionActive(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        try {
            boolean active = params.getOrDefault("active", true);
            journalService.setSectionActive(id, active);
            return ResponseEntity.ok(MapBuilder.of("message", "操作成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/review-rule")
    public ResponseEntity<ReviewRule> getReviewRule() {
        ReviewRule rule = journalService.getReviewRule();
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/review-rule")
    public ResponseEntity<?> updateReviewRule(@RequestBody ReviewRule reviewRule) {
        try {
            ReviewRule updated = journalService.updateReviewRule(reviewRule);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/fee-standard")
    public ResponseEntity<FeeStandard> getFeeStandard() {
        FeeStandard standard = journalService.getFeeStandard();
        if (standard == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(standard);
    }

    @PutMapping("/fee-standard")
    public ResponseEntity<?> updateFeeStandard(@RequestBody FeeStandard feeStandard) {
        try {
            FeeStandard updated = journalService.updateFeeStandard(feeStandard);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
