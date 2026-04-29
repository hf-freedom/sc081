package com.journal.service;

import com.journal.entity.PublicationRecord;
import com.journal.entity.Submission;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicationService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private SubmissionService submissionService;

    public PublicationRecord publish(Long submissionId) {
        Submission submission = submissionService.getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        if (submission.getStatus() != SubmissionStatus.SCHEDULED) {
            throw new IllegalArgumentException("只有排期中的稿件可以出版");
        }
        
        String doi = generateDOI();
        
        PublicationRecord record = PublicationRecord.builder()
                .id(dataStore.getNextPublicationRecordId())
                .submissionId(submissionId)
                .doi(doi)
                .volume(String.valueOf(LocalDate.now().getYear()))
                .issue("1")
                .startPage(1)
                .endPage(submission.getPageCount())
                .publicationDate(LocalDate.now())
                .pdfUrl("/pdf/" + doi)
                .xmlUrl("/xml/" + doi)
                .htmlUrl("/html/" + doi)
                .createdAt(LocalDateTime.now())
                .build();
        
        dataStore.getPublicationRecords().put(record.getId(), record);
        
        submission.setStatus(SubmissionStatus.PUBLISHED);
        submission.setPublishedAt(LocalDateTime.now());
        submission.setPublicationRecordId(record.getId());
        
        return record;
    }

    private String generateDOI() {
        long count = dataStore.getPublicationRecords().size() + 1;
        return "10.1234/jsci." + LocalDate.now().getYear() + "." + String.format("%04d", count);
    }

    public PublicationRecord getPublicationRecordById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("出版记录ID不能为空");
        }
        return dataStore.getPublicationRecords().get(id);
    }

    public PublicationRecord getPublicationRecordBySubmissionId(Long submissionId) {
        return dataStore.getPublicationRecords().values().stream()
                .filter(r -> r.getSubmissionId().equals(submissionId))
                .findFirst()
                .orElse(null);
    }

    public PublicationRecord getPublicationRecordByDOI(String doi) {
        return dataStore.getPublicationRecords().values().stream()
                .filter(r -> doi.equals(r.getDoi()))
                .findFirst()
                .orElse(null);
    }

    public List<PublicationRecord> getAllPublicationRecords() {
        return new ArrayList<>(dataStore.getPublicationRecords().values());
    }

    public List<PublicationRecord> getPublicationRecordsByYear(int year) {
        return dataStore.getPublicationRecords().values().stream()
                .filter(r -> r.getPublicationDate() != null && 
                        r.getPublicationDate().getYear() == year)
                .collect(Collectors.toList());
    }
}
