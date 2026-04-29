package com.journal.repository;

import com.journal.entity.*;
import com.journal.entity.enums.PaymentStatus;
import com.journal.entity.enums.ReviewResult;
import com.journal.entity.enums.SubmissionStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryDataStore {

    private final AtomicLong authorIdGenerator = new AtomicLong(1);
    private final AtomicLong reviewerIdGenerator = new AtomicLong(1);
    private final AtomicLong journalIdGenerator = new AtomicLong(1);
    private final AtomicLong sectionIdGenerator = new AtomicLong(1);
    private final AtomicLong submissionIdGenerator = new AtomicLong(1);
    private final AtomicLong reviewIdGenerator = new AtomicLong(1);
    private final AtomicLong revisionIdGenerator = new AtomicLong(1);
    private final AtomicLong feeIdGenerator = new AtomicLong(1);
    private final AtomicLong publicationRecordIdGenerator = new AtomicLong(1);
    private final AtomicLong reportIdGenerator = new AtomicLong(1);

    private final Map<Long, Author> authors = new ConcurrentHashMap<>();
    private final Map<Long, Reviewer> reviewers = new ConcurrentHashMap<>();
    private final Map<Long, Journal> journals = new ConcurrentHashMap<>();
    private final Map<Long, Section> sections = new ConcurrentHashMap<>();
    private final Map<Long, Submission> submissions = new ConcurrentHashMap<>();
    private final Map<Long, Review> reviews = new ConcurrentHashMap<>();
    private final Map<Long, Revision> revisions = new ConcurrentHashMap<>();
    private final Map<Long, PublicationFee> fees = new ConcurrentHashMap<>();
    private final Map<Long, PublicationRecord> publicationRecords = new ConcurrentHashMap<>();
    private final Map<Long, Report> reports = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initBaseData();
    }

    private void initBaseData() {
        Section section1 = Section.builder()
                .id(sectionIdGenerator.getAndIncrement())
                .name("研究论文")
                .code("RESEARCH")
                .description("原创性研究论文")
                .active(true)
                .basePageFee(new BigDecimal("200"))
                .maxPages(20)
                .minPages(5)
                .requiredFormat("APA")
                .build();
        sections.put(section1.getId(), section1);

        Section section2 = Section.builder()
                .id(sectionIdGenerator.getAndIncrement())
                .name("综述论文")
                .code("REVIEW")
                .description("综述性论文")
                .active(true)
                .basePageFee(new BigDecimal("180"))
                .maxPages(30)
                .minPages(8)
                .requiredFormat("APA")
                .build();
        sections.put(section2.getId(), section2);

        Section section3 = Section.builder()
                .id(sectionIdGenerator.getAndIncrement())
                .name("短文报告")
                .code("SHORT")
                .description("简短研究报告")
                .active(true)
                .basePageFee(new BigDecimal("150"))
                .maxPages(8)
                .minPages(2)
                .requiredFormat("APA")
                .build();
        sections.put(section3.getId(), section3);

        ReviewRule reviewRule = ReviewRule.builder()
                .reviewerCount(2)
                .reviewDays(30)
                .doubleBlind(true)
                .allowConflictReject(true)
                .reminderDaysBeforeDeadline(7)
                .build();

        FeeStandard feeStandard = FeeStandard.builder()
                .pageFee(new BigDecimal("200"))
                .freePages(8)
                .extraPageFee(new BigDecimal("300"))
                .colorFee(new BigDecimal("500"))
                .openAccessFee(new BigDecimal("2000"))
                .latePaymentPenalty(new BigDecimal("100"))
                .build();

        Journal journal = Journal.builder()
                .id(journalIdGenerator.getAndIncrement())
                .name("科学研究期刊")
                .abbreviation("JSCI")
                .issn("1234-5678")
                .publisher("科学出版社")
                .description("综合性科学研究期刊")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .sections(new ArrayList<>(Arrays.asList(section1, section2, section3)))
                .reviewRule(reviewRule)
                .feeStandard(feeStandard)
                .build();
        journals.put(journal.getId(), journal);

        Author author1 = Author.builder()
                .id(authorIdGenerator.getAndIncrement())
                .name("张三")
                .email("zhangsan@example.com")
                .phone("13800138001")
                .institution("北京大学")
                .address("北京市海淀区")
                .blacklisted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .submissionIds(new ArrayList<>())
                .build();
        authors.put(author1.getId(), author1);

        Author author2 = Author.builder()
                .id(authorIdGenerator.getAndIncrement())
                .name("李四")
                .email("lisi@example.com")
                .phone("13800138002")
                .institution("清华大学")
                .address("北京市海淀区")
                .blacklisted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .submissionIds(new ArrayList<>())
                .build();
        authors.put(author2.getId(), author2);

        Author author3 = Author.builder()
                .id(authorIdGenerator.getAndIncrement())
                .name("王五")
                .email("wangwu@example.com")
                .phone("13800138003")
                .institution("复旦大学")
                .address("上海市杨浦区")
                .blacklisted(true)
                .blacklistReason("多次抄袭行为")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .submissionIds(new ArrayList<>())
                .build();
        authors.put(author3.getId(), author3);

        Reviewer reviewer1 = Reviewer.builder()
                .id(reviewerIdGenerator.getAndIncrement())
                .name("陈教授")
                .email("chen@example.com")
                .phone("13900139001")
                .institution("中国科学院")
                .researchField("计算机科学")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .conflictOfInterestInstitutions(new ArrayList<>())
                .reviewIds(new ArrayList<>())
                .build();
        reviewers.put(reviewer1.getId(), reviewer1);

        Reviewer reviewer2 = Reviewer.builder()
                .id(reviewerIdGenerator.getAndIncrement())
                .name("刘教授")
                .email("liu@example.com")
                .phone("13900139002")
                .institution("浙江大学")
                .researchField("人工智能")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .conflictOfInterestInstitutions(Arrays.asList("北京大学"))
                .reviewIds(new ArrayList<>())
                .build();
        reviewers.put(reviewer2.getId(), reviewer2);

        Reviewer reviewer3 = Reviewer.builder()
                .id(reviewerIdGenerator.getAndIncrement())
                .name("王教授")
                .email("wangprof@example.com")
                .phone("13900139003")
                .institution("上海交通大学")
                .researchField("数据科学")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .conflictOfInterestInstitutions(new ArrayList<>())
                .reviewIds(new ArrayList<>())
                .build();
        reviewers.put(reviewer3.getId(), reviewer3);
    }

    public long getNextAuthorId() { return authorIdGenerator.getAndIncrement(); }
    public long getNextReviewerId() { return reviewerIdGenerator.getAndIncrement(); }
    public long getNextJournalId() { return journalIdGenerator.getAndIncrement(); }
    public long getNextSectionId() { return sectionIdGenerator.getAndIncrement(); }
    public long getNextSubmissionId() { return submissionIdGenerator.getAndIncrement(); }
    public long getNextReviewId() { return reviewIdGenerator.getAndIncrement(); }
    public long getNextRevisionId() { return revisionIdGenerator.getAndIncrement(); }
    public long getNextFeeId() { return feeIdGenerator.getAndIncrement(); }
    public long getNextPublicationRecordId() { return publicationRecordIdGenerator.getAndIncrement(); }
    public long getNextReportId() { return reportIdGenerator.getAndIncrement(); }

    public Map<Long, Author> getAuthors() { return authors; }
    public Map<Long, Reviewer> getReviewers() { return reviewers; }
    public Map<Long, Journal> getJournals() { return journals; }
    public Map<Long, Section> getSections() { return sections; }
    public Map<Long, Submission> getSubmissions() { return submissions; }
    public Map<Long, Review> getReviews() { return reviews; }
    public Map<Long, Revision> getRevisions() { return revisions; }
    public Map<Long, PublicationFee> getFees() { return fees; }
    public Map<Long, PublicationRecord> getPublicationRecords() { return publicationRecords; }
    public Map<Long, Report> getReports() { return reports; }
}
