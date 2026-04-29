package com.journal.service;

import com.journal.entity.Revision;
import com.journal.entity.Submission;
import com.journal.entity.enums.SubmissionStatus;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RevisionService {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private JournalService journalService;

    public Revision getRevisionById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("修改稿ID不能为空");
        }
        return dataStore.getRevisions().get(id);
    }

    public List<Revision> getRevisionsBySubmissionId(Long submissionId) {
        return dataStore.getRevisions().values().stream()
                .filter(r -> r.getSubmissionId().equals(submissionId))
                .collect(Collectors.toList());
    }

    public List<Revision> getOverdueRevisions() {
        LocalDateTime now = LocalDateTime.now();
        return dataStore.getRevisions().values().stream()
                .filter(r -> "REQUESTED".equals(r.getStatus()))
                .filter(r -> r.getDueDate() != null && r.getDueDate().isBefore(now))
                .collect(Collectors.toList());
    }

    public Revision submitRevision(Long revisionId, String responseLetter, String revisedManuscriptFile) {
        Revision revision = getRevisionById(revisionId);
        if (revision == null) {
            throw new IllegalArgumentException("修改稿不存在");
        }
        if (!"REQUESTED".equals(revision.getStatus())) {
            throw new IllegalArgumentException("该修改稿状态不允许提交");
        }
        
        revision.setSubmittedAt(LocalDateTime.now());
        revision.setResponseLetter(responseLetter);
        revision.setRevisedManuscriptFile(revisedManuscriptFile);
        revision.setStatus("SUBMITTED");
        
        Submission submission = submissionService.getSubmissionById(revision.getSubmissionId());
        if (submission != null) {
            submission.setStatus(SubmissionStatus.REVIEWING);
            submission.setReviewingStartedAt(LocalDateTime.now());
        }
        
        return revision;
    }

    public Revision createNewRevision(Long submissionId, boolean isMinor, String instructions) {
        Submission submission = submissionService.getSubmissionById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("投稿不存在");
        }
        
        List<Revision> existingRevisions = getRevisionsBySubmissionId(submissionId);
        int nextRevisionNumber = existingRevisions.size() + 1;
        
        int revisionDays = isMinor ? 14 : 30;
        
        Revision revision = Revision.builder()
                .id(dataStore.getNextRevisionId())
                .submissionId(submissionId)
                .revisionNumber(nextRevisionNumber)
                .requestedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(revisionDays))
                .instructions(instructions)
                .isMinor(isMinor)
                .status("REQUESTED")
                .build();
        
        dataStore.getRevisions().put(revision.getId(), revision);
        submission.getRevisionIds().add(revision.getId());
        
        return revision;
    }

    public boolean isRevisionOverdue(Long revisionId) {
        Revision revision = getRevisionById(revisionId);
        if (revision == null) {
            return false;
        }
        if (!"REQUESTED".equals(revision.getStatus())) {
            return false;
        }
        if (revision.getDueDate() == null) {
            return false;
        }
        return revision.getDueDate().isBefore(LocalDateTime.now());
    }
}
