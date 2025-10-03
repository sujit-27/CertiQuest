package com.web.CertiQuest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard")
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String userId;
    private String userName;
    private String avatarUrl;

    private int totalPoints;
    private int quizzesAttempted;
    private double averagePercentage;

    private int score;
    private double percentage;

    private LocalDateTime attemptedAt;

    @Transient
    private int rank;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getQuizzesAttempted() { return quizzesAttempted; }
    public void setQuizzesAttempted(int quizzesAttempted) { this.quizzesAttempted = quizzesAttempted; }

    public double getAveragePercentage() { return averagePercentage; }
    public void setAveragePercentage(double averagePercentage) { this.averagePercentage = averagePercentage; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
