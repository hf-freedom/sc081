package com.journal.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.journal.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationFee {
    private Long id;
    private Long submissionId;
    private String invoiceNumber;
    private BigDecimal baseFee;
    private BigDecimal extraPageFee;
    private BigDecimal colorFee;
    private BigDecimal openAccessFee;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private PaymentStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;
    private String paymentMethod;
    private String transactionId;
    private String notes;
}
