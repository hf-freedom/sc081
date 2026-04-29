package com.journal.controller;

import com.journal.entity.PublicationRecord;
import com.journal.service.PublicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/publications")
@CrossOrigin(origins = "http://localhost:3002")
public class PublicationController {

    @Autowired
    private PublicationService publicationService;

    @GetMapping
    public ResponseEntity<List<PublicationRecord>> getAllPublicationRecords() {
        List<PublicationRecord> records = publicationService.getAllPublicationRecords();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<PublicationRecord>> getPublicationRecordsByYear(@PathVariable int year) {
        List<PublicationRecord> records = publicationService.getPublicationRecordsByYear(year);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationRecord> getPublicationRecordById(@PathVariable Long id) {
        PublicationRecord record = publicationService.getPublicationRecordById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @GetMapping("/doi/{doi}")
    public ResponseEntity<PublicationRecord> getPublicationRecordByDOI(@PathVariable String doi) {
        PublicationRecord record = publicationService.getPublicationRecordByDOI(doi);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<PublicationRecord> getPublicationRecordBySubmissionId(@PathVariable Long submissionId) {
        PublicationRecord record = publicationService.getPublicationRecordBySubmissionId(submissionId);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @PostMapping
    public ResponseEntity<?> publish(@RequestBody Map<String, Long> params) {
        try {
            Long submissionId = params.get("submissionId");
            PublicationRecord record = publicationService.publish(submissionId);
            return ResponseEntity.ok(record);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
