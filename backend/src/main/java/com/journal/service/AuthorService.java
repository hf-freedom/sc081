package com.journal.service;

import com.journal.entity.Author;
import com.journal.entity.Submission;
import com.journal.repository.InMemoryDataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    @Autowired
    private InMemoryDataStore dataStore;

    public Author createAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("作者信息不能为空");
        }
        author.setId(dataStore.getNextAuthorId());
        author.setCreatedAt(LocalDateTime.now());
        author.setUpdatedAt(LocalDateTime.now());
        author.setBlacklisted(false);
        author.setSubmissionIds(new ArrayList<>());
        
        boolean emailExists = dataStore.getAuthors().values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(author.getEmail()));
        if (emailExists) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        
        dataStore.getAuthors().put(author.getId(), author);
        return author;
    }

    public Author getAuthorById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("作者ID不能为空");
        }
        return dataStore.getAuthors().get(id);
    }

    public List<Author> getAllAuthors() {
        return new ArrayList<>(dataStore.getAuthors().values());
    }

    public Author updateAuthor(Long id, Author author) {
        if (id == null || author == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Author existing = dataStore.getAuthors().get(id);
        if (existing == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        
        if (author.getName() != null) existing.setName(author.getName());
        if (author.getEmail() != null) {
            boolean emailExists = dataStore.getAuthors().values().stream()
                    .anyMatch(a -> !a.getId().equals(id) && a.getEmail().equalsIgnoreCase(author.getEmail()));
            if (emailExists) {
                throw new IllegalArgumentException("该邮箱已被使用");
            }
            existing.setEmail(author.getEmail());
        }
        if (author.getPhone() != null) existing.setPhone(author.getPhone());
        if (author.getInstitution() != null) existing.setInstitution(author.getInstitution());
        if (author.getAddress() != null) existing.setAddress(author.getAddress());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return existing;
    }

    public void setBlacklist(Long id, boolean blacklisted, String reason) {
        Author author = getAuthorById(id);
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        author.setBlacklisted(blacklisted);
        author.setBlacklistReason(blacklisted ? reason : null);
        author.setUpdatedAt(LocalDateTime.now());
    }

    public List<Submission> getAuthorSubmissions(Long authorId) {
        Author author = getAuthorById(authorId);
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        return author.getSubmissionIds().stream()
                .map(dataStore.getSubmissions()::get)
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }

    public void addSubmissionToAuthor(Long authorId, Long submissionId) {
        Author author = getAuthorById(authorId);
        if (author == null) {
            throw new IllegalArgumentException("作者不存在");
        }
        if (!author.getSubmissionIds().contains(submissionId)) {
            author.getSubmissionIds().add(submissionId);
        }
    }
}
