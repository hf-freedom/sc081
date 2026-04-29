package com.journal.controller;

import com.journal.entity.Revision;
import com.journal.service.RevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/revisions")
@CrossOrigin(origins = "http://localhost:3002")
public class RevisionController {

    @Autowired
    private RevisionService revisionService;

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<List<Revision>> getRevisionsBySubmissionId(@PathVariable Long submissionId) {
        List<Revision> revisions = revisionService.getRevisionsBySubmissionId(submissionId);
        return ResponseEntity.ok(revisions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Revision> getRevisionById(@PathVariable Long id) {
        Revision revision = revisionService.getRevisionById(id);
        if (revision == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(revision);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submitRevision(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String responseLetter = params.get("responseLetter");
            String revisedManuscriptFile = params.get("revisedManuscriptFile");
            Revision updated = revisionService.submitRevision(id, responseLetter, revisedManuscriptFile);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Revision>> getOverdueRevisions() {
        List<Revision> revisions = revisionService.getOverdueRevisions();
        return ResponseEntity.ok(revisions);
    }

    @PostMapping
    public ResponseEntity<?> createNewRevision(@RequestBody Map<String, Object> params) {
        try {
            Long submissionId = Long.valueOf(params.get("submissionId").toString());
            Boolean isMinor = (Boolean) params.getOrDefault("isMinor", true);
            String instructions = (String) params.get("instructions");
            Revision created = revisionService.createNewRevision(submissionId, isMinor, instructions);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
