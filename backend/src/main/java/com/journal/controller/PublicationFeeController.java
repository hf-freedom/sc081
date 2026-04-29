package com.journal.controller;

import com.journal.entity.PublicationFee;
import com.journal.entity.enums.PaymentStatus;
import com.journal.service.PublicationFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.journal.util.MapBuilder;

@RestController
@RequestMapping("/api/fees")
@CrossOrigin(origins = "http://localhost:3002")
public class PublicationFeeController {

    @Autowired
    private PublicationFeeService feeService;

    @GetMapping
    public ResponseEntity<List<PublicationFee>> getAllFees() {
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PublicationFee>> getFeesByStatus(@PathVariable PaymentStatus status) {
        List<PublicationFee> fees = feeService.getFeesByStatus(status);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<PublicationFee> getFeeBySubmissionId(@PathVariable Long submissionId) {
        PublicationFee fee = feeService.getFeeBySubmissionId(submissionId);
        if (fee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fee);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationFee> getFeeById(@PathVariable Long id) {
        PublicationFee fee = feeService.getFeeById(id);
        if (fee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fee);
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> recordPayment(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            BigDecimal amount = new BigDecimal(params.get("amount").toString());
            String paymentMethod = (String) params.get("paymentMethod");
            String transactionId = (String) params.get("transactionId");
            PublicationFee updated = feeService.recordPayment(id, amount, paymentMethod, transactionId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/apply-penalty")
    public ResponseEntity<?> applyLatePaymentPenalty(@PathVariable Long id) {
        try {
            PublicationFee updated = feeService.applyLatePaymentPenalty(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MapBuilder.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<PublicationFee>> getOverdueFees() {
        List<PublicationFee> fees = feeService.getOverdueFees();
        return ResponseEntity.ok(fees);
    }
}
