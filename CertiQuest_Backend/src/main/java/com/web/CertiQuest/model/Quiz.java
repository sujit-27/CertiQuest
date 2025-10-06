package com.web.CertiQuest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name = "quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String category;
    private String difficulty;
    private String createdBy;
    private Instant createdAt;
    private LocalDate expiryDate;
    private Integer noOfQuestions;

    // ✅ Keep orphanRemoval = true to clean up unlinked questions,
    // but use helper methods instead of replacing the list directly.
    @OneToMany(mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<QuizQuestion> questions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "quiz_participants", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "participant_id")
    private List<String> participants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (expiryDate == null) expiryDate = LocalDate.now().plusDays(7);
    }

    // ==========================
    // 🔹 Helper Methods — CRITICAL for JPA consistency
    // ==========================

    /**
     * Adds a question to this quiz and sets the quiz reference
     * on the question side to keep the relationship in sync.
     */
    public void addQuestion(QuizQuestion question) {
        if (question == null) return;
        if (!this.questions.contains(question)) {
            this.questions.add(question);
            question.setQuiz(this);
        }
    }

    /**
     * Removes a question from this quiz and clears its quiz reference.
     * Using this avoids orphan deletion issues when updating quizzes.
     */
    public void removeQuestion(QuizQuestion question) {
        if (question == null) return;
        if (this.questions.remove(question)) {
            question.setQuiz(null);
        }
    }

    /**
     * Removes all existing questions safely while maintaining
     * bidirectional consistency.
     */
    public void clearQuestions() {
        for (Iterator<QuizQuestion> iterator = questions.iterator(); iterator.hasNext(); ) {
            QuizQuestion question = iterator.next();
            question.setQuiz(null);
            iterator.remove();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getNoOfQuestions() {
        return noOfQuestions;
    }

    public void setNoOfQuestions(Integer noOfQuestions) {
        this.noOfQuestions = noOfQuestions;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", expiryDate=" + expiryDate +
                ", noOfQuestions=" + noOfQuestions +
                ", participants=" + participants +
                '}';
    }

}
