package com.journal.service;

import com.journal.entity.FeeStandard;
import com.journal.entity.PublicationFee;
import com.journal.entity.Section;
import com.journal.entity.Submission;
import com.journal.entity.enums.PaymentStatus;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicationFeeService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private JournalService journalService;

    @Autowired
    private SubmissionService submissionService;

    public PublicationFee createFee(Long submissionId) {
        Submission submission = submissionService.getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        
        FeeStandard feeStandard = journalService.getFeeStandard();
        Section section = journalService.getSectionById(submission.getSectionId());
        
        BigDecimal baseFee = BigDecimal.ZERO;
        BigDecimal extraPageFee = BigDecimal.ZERO;
        BigDecimal colorFee = BigDecimal.ZERO;
        BigDecimal openAccessFee = BigDecimal.ZERO;
        
        if (section != null && section.getBasePageFee() != null) {
            baseFee = section.getBasePageFee();
        } else if (feeStandard != null && feeStandard.getPageFee() != null) {
            baseFee = feeStandard.getPageFee();
        }
        
        int freePages = feeStandard != null ? feeStandard.getFreePages() : 8;
        int extraPages = Math.max(0, submission.getPageCount() - freePages);
        if (extraPages > 0 && feeStandard != null && feeStandard.getExtraPageFee() != null) {
            extraPageFee = feeStandard.getExtraPageFee().multiply(BigDecimal.valueOf(extraPages));
        }
        
        if (submission.isColorPrint() && feeStandard != null && feeStandard.getColorFee() != null) {
            colorFee = feeStandard.getColorFee();
        }
        
        if (submission.isOpenAccess() && feeStandard != null && feeStandard.getOpenAccessFee() != null) {
            openAccessFee = feeStandard.getOpenAccessFee();
        }
        
        BigDecimal totalAmount = baseFee.add(extraPageFee).add(colorFee).add(openAccessFee);
        
        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + 
                String.format("%04d", submissionId);
        
        PublicationFee fee = PublicationFee.builder()
                .id(dataStore.getNextFeeId())
                .submissionId(submissionId)
                .invoiceNumber(invoiceNumber)
                .baseFee(baseFee)
                .extraPageFee(extraPageFee)
                .colorFee(colorFee)
                .openAccessFee(openAccessFee)
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .build();
        
        dataStore.getFees().put(fee.getId(), fee);
        submission.setFeeId(fee.getId());
        submission.setStatus(SubmissionStatus.FEE_PENDING);
        
        return fee;
    }

    public PublicationFee getFeeById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("费用ID不能为空");
        }
        return dataStore.getFees().get(id);
    }

    public PublicationFee getFeeBySubmissionId(Long submissionId) {
        return dataStore.getFees().values().stream()
                .filter(f -> f.getSubmissionId().equals(submissionId))
                .findFirst()
                .orElse(null);
    }

    public List<PublicationFee> getFeesByStatus(PaymentStatus status) {
        return dataStore.getFees().values().stream()
                .filter(f -> f.getStatus() == status)
                .collect(Collectors.toList());
    }

    public PublicationFee recordPayment(Long feeId, BigDecimal amount, String paymentMethod, String transactionId) {
        PublicationFee fee = getFeeById(feeId);
        if (fee == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        if (fee.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("该费用已支付");
        }
        
        BigDecimal newPaidAmount = fee.getPaidAmount().add(amount);
        
        if (newPaidAmount.compareTo(fee.getTotalAmount()) >= 0) {
            fee.setStatus(PaymentStatus.PAID);
            fee.setPaidAt(LocalDateTime.now());
            
            Submission submission = submissionService.getSubmissionById(fee.getSubmissionId());
            if (submission != null) {
                submission.setStatus(SubmissionStatus.FEE_PAID);
                submission.setScheduledAt(LocalDateTime.now());
                submission.setStatus(SubmissionStatus.SCHEDULED);
            }
        }
        
        fee.setPaidAmount(newPaidAmount);
        fee.setPaymentMethod(paymentMethod);
        fee.setTransactionId(transactionId);
        
        return fee;
    }

    public PublicationFee applyLatePaymentPenalty(Long feeId) {
        PublicationFee fee = getFeeById(feeId);
        if (fee == null) {
            throw new IllegalArgumentException("费用记录不存在");
        }
        if (fee.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("只有待支付状态的费用可以应用滞纳金");
        }
        if (fee.getDueDate() == null || fee.getDueDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("费用未逾期，无法应用滞纳金");
        }
        
        FeeStandard feeStandard = journalService.getFeeStandard();
        if (feeStandard != null && feeStandard.getLatePaymentPenalty() != null) {
            fee.setTotalAmount(fee.getTotalAmount().add(feeStandard.getLatePaymentPenalty()));
        }
        
        return fee;
    }

    public List<PublicationFee> getOverdueFees() {
        LocalDateTime now = LocalDateTime.now();
        return dataStore.getFees().values().stream()
                .filter(f -> f.getStatus() == PaymentStatus.PENDING)
                .filter(f -> f.getDueDate() != null && f.getDueDate().isBefore(now))
                .collect(Collectors.toList());
    }
}
