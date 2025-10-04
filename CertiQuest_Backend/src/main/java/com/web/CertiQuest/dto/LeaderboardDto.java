package com.web.CertiQuest.dto;

public class LeaderboardDto {
    private String userId;
    private String userName;
    private String avatarUrl;
    private Long totalPoints;          // Use wrapper Long for JPQL compatibility
    private Long quizzesAttempted;     // Use wrapper Long
    private Double averagePercentage;  // Double wrapper
    private int rank; // Additional field for ranking

    public LeaderboardDto(String userId, String userName, String avatarUrl,
                          Long totalPoints, Long quizzesAttempted, Double averagePercentage) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.totalPoints = totalPoints;
        this.quizzesAttempted = quizzesAttempted;
        this.averagePercentage = averagePercentage;
    }

    // Getter and setter for rank
    public void setRank(int rank) { this.rank = rank; }
    public int getRank() { return rank; }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAvatarUrl() { return avatarUrl; }
    public Long getTotalPoints() { return totalPoints; }
    public Long getQuizzesAttempted() { return quizzesAttempted; }
    public Double getAveragePercentage() { return averagePercentage; }
}
