package com.journal.controller;

import com.journal.entity.Report;
import com.journal.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3002")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<Report>> getReportsByYear(@PathVariable int year) {
        List<Report> reports = reportService.getReportsByYear(year);
        return ResponseEntity.ok(reports);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateMonthlyReport(@RequestBody Map<String, Integer> params) {
        try {
            int year = params.getOrDefault("year", java.time.LocalDate.now().getYear());
            int month = params.getOrDefault("month", java.time.LocalDate.now().getMonthValue());
            Report report = reportService.generateMonthlyReport(year, month);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }
}
