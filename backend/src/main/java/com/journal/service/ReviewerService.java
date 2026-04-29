package com.journal.service;

import com.journal.entity.Review;
import com.journal.entity.Reviewer;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewerService {

    @Autowired
    private InMemoryDataStore dataStore;

    public Reviewer createReviewer(Reviewer reviewer) {
        if (reviewer == null) {
            throw new IllegalArgumentException("审稿人信息不能为空");
        }
        reviewer.setId(dataStore.getNextReviewerId());
        reviewer.setCreatedAt(LocalDateTime.now());
        reviewer.setUpdatedAt(LocalDateTime.now());
        reviewer.setActive(true);
        
        if (reviewer.getConflictOfInterestInstitutions() == null) {
            reviewer.setConflictOfInterestInstitutions(new ArrayList<>());
        }
        if (reviewer.getReviewIds() == null) {
            reviewer.setReviewIds(new ArrayList<>());
        }
        
        dataStore.getReviewers().put(reviewer.getId(), reviewer);
        return reviewer;
    }

    public Reviewer getReviewerById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审稿人ID不能为空");
        }
        return dataStore.getReviewers().get(id);
    }

    public List<Reviewer> getAllReviewers() {
        return new ArrayList<>(dataStore.getReviewers().values());
    }

    public List<Reviewer> getActiveReviewers() {
        return dataStore.getReviewers().values().stream()
                .filter(Reviewer::isActive)
                .collect(Collectors.toList());
    }

    public Reviewer updateReviewer(Long id, Reviewer reviewer) {
        if (id == null || reviewer == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Reviewer existing = dataStore.getReviewers().get(id);
        if (existing == null) {
            throw new IllegalArgumentException("审稿人不存在");
        }
        
        if (reviewer.getName() != null) existing.setName(reviewer.getName());
        if (reviewer.getEmail() != null) existing.setEmail(reviewer.getEmail());
        if (reviewer.getPhone() != null) existing.setPhone(reviewer.getPhone());
        if (reviewer.getInstitution() != null) existing.setInstitution(reviewer.getInstitution());
        if (reviewer.getResearchField() != null) existing.setResearchField(reviewer.getResearchField());
        if (reviewer.getConflictOfInterestInstitutions() != null) {
            existing.setConflictOfInterestInstitutions(reviewer.getConflictOfInterestInstitutions());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        
        return existing;
    }

    public void setActive(Long id, boolean active) {
        Reviewer reviewer = getReviewerById(id);
        if (reviewer == null) {
            throw new IllegalArgumentException("审稿人不存在");
        }
        reviewer.setActive(active);
        reviewer.setUpdatedAt(LocalDateTime.now());
    }

    public boolean hasConflictOfInterest(Long reviewerId, String institution) {
        Reviewer reviewer = getReviewerById(reviewerId);
        if (reviewer == null) {
            throw new IllegalArgumentException("审稿人不存在");
        }
        if (institution == null || institution.trim().isEmpty()) {
            return false;
        }
        
        String trimmedInstitution = institution.trim();
        
        if (reviewer.getInstitution() != null && reviewer.getInstitution().trim().equals(trimmedInstitution)) {
            return true;
        }
        
        return reviewer.getConflictOfInterestInstitutions().stream()
                .anyMatch(ci -> ci != null && ci.trim().equals(trimmedInstitution));
    }

    public List<Reviewer> findEligibleReviewers(String authorInstitution, String researchField) {
        List<Reviewer> eligible = getActiveReviewers();
        
        if (authorInstitution != null && !authorInstitution.isEmpty()) {
            eligible = eligible.stream()
                    .filter(r -> !hasConflictOfInterest(r.getId(), authorInstitution))
                    .collect(Collectors.toList());
        }
        
        if (researchField != null && !researchField.isEmpty()) {
            List<Reviewer> matchedByField = eligible.stream()
                    .filter(r -> r.getResearchField() != null && 
                            (r.getResearchField().toLowerCase().contains(researchField.toLowerCase()) ||
                             researchField.toLowerCase().contains(r.getResearchField().toLowerCase())))
                    .collect(Collectors.toList());
            
            if (!matchedByField.isEmpty()) {
                return matchedByField;
            }
        }
        
        return eligible;
    }

    public List<Review> getReviewerReviews(Long reviewerId) {
        Reviewer reviewer = getReviewerById(reviewerId);
        if (reviewer == null) {
            throw new IllegalArgumentException("审稿人不存在");
        }
        return reviewer.getReviewIds().stream()
                .map(dataStore.getReviews()::get)
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    public void addReviewToReviewer(Long reviewerId, Long reviewId) {
        Reviewer reviewer = getReviewerById(reviewerId);
        if (reviewer == null) {
            throw new IllegalArgumentException("审稿人不存在");
        }
        if (!reviewer.getReviewIds().contains(reviewId)) {
            reviewer.getReviewIds().add(reviewId);
        }
    }
}
