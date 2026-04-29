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
public class Section {
    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private BigDecimal basePageFee;
    private int maxPages;
    private int minPages;
    private String requiredFormat;
}
