package com.journal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeStandard {
    private BigDecimal pageFee;
    private int freePages;
    private BigDecimal extraPageFee;
    private BigDecimal colorFee;
    private BigDecimal openAccessFee;
    private BigDecimal latePaymentPenalty;
}
