package com.journal.controller;

import com.journal.entity.Author;
import com.journal.entity.Submission;
import com.journal.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "http://localhost:3002")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(author);
    }

    @PostMapping
    public ResponseEntity<?> createAuthor(@RequestBody Author author) {
        try {
            Author created = authorService.createAuthor(author);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        try {
            Author updated = authorService.updateAuthor(id, author);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/blacklist")
    public ResponseEntity<?> setBlacklist(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            boolean blacklisted = (Boolean) params.getOrDefault("blacklisted", true);
            String reason = (String) params.get("reason");
            authorService.setBlacklist(id, blacklisted, reason);
            return ResponseEntity.ok(MapBuilder.of("message", "操作成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<Submission>> getAuthorSubmissions(@PathVariable Long id) {
        try {
            List<Submission> submissions = authorService.getAuthorSubmissions(id);
            return ResponseEntity.ok(submissions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
