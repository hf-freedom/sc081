package com.journal.service;

import com.journal.entity.FeeStandard;
import com.journal.entity.Journal;
import com.journal.entity.ReviewRule;
import com.journal.entity.Section;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JournalService {

    @Autowired
    private InMemoryDataStore dataStore;

    public Journal getMainJournal() {
        return dataStore.getJournals().values().stream()
                .filter(Journal::isActive)
                .findFirst()
                .orElse(null);
    }

    public Section getSectionById(Long sectionId) {
        return dataStore.getSections().get(sectionId);
    }

    public List<Section> getAllSections() {
        return new ArrayList<>(dataStore.getSections().values());
    }

    public List<Section> getActiveSections() {
        return dataStore.getSections().values().stream()
                .filter(Section::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public Section createSection(Section section) {
        if (section == null) {
            throw new IllegalArgumentException("栏目信息不能为空");
        }
        section.setId(dataStore.getNextSectionId());
        section.setActive(true);
        
        boolean codeExists = dataStore.getSections().values().stream()
                .anyMatch(s -> s.getCode().equalsIgnoreCase(section.getCode()));
        if (codeExists) {
            throw new IllegalArgumentException("栏目代码已存在");
        }
        
        dataStore.getSections().put(section.getId(), section);
        
        Journal journal = getMainJournal();
        if (journal != null) {
            journal.getSections().add(section);
            journal.setUpdatedAt(LocalDateTime.now());
        }
        
        return section;
    }

    public Section updateSection(Long id, Section section) {
        if (id == null || section == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Section existing = dataStore.getSections().get(id);
        if (existing == null) {
            throw new IllegalArgumentException("栏目不存在");
        }
        
        if (section.getName() != null) existing.setName(section.getName());
        if (section.getDescription() != null) existing.setDescription(section.getDescription());
        if (section.getBasePageFee() != null) existing.setBasePageFee(section.getBasePageFee());
        if (section.getMaxPages() > 0) existing.setMaxPages(section.getMaxPages());
        if (section.getMinPages() > 0) existing.setMinPages(section.getMinPages());
        if (section.getRequiredFormat() != null) existing.setRequiredFormat(section.getRequiredFormat());
        if (section.getCode() != null) {
            boolean codeExists = dataStore.getSections().values().stream()
                    .anyMatch(s -> !s.getId().equals(id) && s.getCode().equalsIgnoreCase(section.getCode()));
            if (codeExists) {
                throw new IllegalArgumentException("栏目代码已存在");
            }
            existing.setCode(section.getCode());
        }
        
        return existing;
    }

    public void setSectionActive(Long id, boolean active) {
        Section section = getSectionById(id);
        if (section == null) {
            throw new IllegalArgumentException("栏目不存在");
        }
        section.setActive(active);
    }

    public ReviewRule getReviewRule() {
        Journal journal = getMainJournal();
        return journal != null ? journal.getReviewRule() : null;
    }

    public ReviewRule updateReviewRule(ReviewRule reviewRule) {
        if (reviewRule == null) {
            throw new IllegalArgumentException("审稿规则不能为空");
        }
        Journal journal = getMainJournal();
        if (journal == null) {
            throw new IllegalArgumentException("期刊不存在");
        }
        journal.setReviewRule(reviewRule);
        journal.setUpdatedAt(LocalDateTime.now());
        return reviewRule;
    }

    public FeeStandard getFeeStandard() {
        Journal journal = getMainJournal();
        return journal != null ? journal.getFeeStandard() : null;
    }

    public FeeStandard updateFeeStandard(FeeStandard feeStandard) {
        if (feeStandard == null) {
            throw new IllegalArgumentException("版面费标准不能为空");
        }
        Journal journal = getMainJournal();
        if (journal == null) {
            throw new IllegalArgumentException("期刊不存在");
        }
        journal.setFeeStandard(feeStandard);
        journal.setUpdatedAt(LocalDateTime.now());
        return feeStandard;
    }
}
