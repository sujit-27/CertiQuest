package com.web.CertiQuest.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id")
    private int quizId;
    private String userId;           // User who earned the certificate
    private String userName;         // Optional: store user name
    private int score;               // User's score
    private int totalQuestions;      // Total questions in quiz
    private double percentage;       // Score percentage
    private String quizTitle;        // Title of the quiz
    private String difficulty;       // Difficulty of the quiz (medium/hard)
    private LocalDate issuedAt;      // When certificate was issued
    private String certificateUrl;   // Path to PDF file

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; updatePercentage(); }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; updatePercentage(); }

    public double getPercentage() { return percentage; }
    private void updatePercentage() {
        if (totalQuestions > 0) {
            this.percentage = (score * 100.0) / totalQuestions;
        } else {
            this.percentage = 0;
        }
    }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public LocalDate getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDate issuedAt) { this.issuedAt = issuedAt; }

    public String getCertificateUrl() { return certificateUrl; }
    public void setCertificateUrl(String certificateUrl) { this.certificateUrl = certificateUrl; }
}
