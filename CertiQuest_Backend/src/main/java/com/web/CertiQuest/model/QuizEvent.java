package com.web.CertiQuest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;

public class QuizEvent implements Serializable {

    @JsonProperty("action")
    private String eventType; // CREATED, UPDATED, DELETED

    private int quizId;
    private String title;
    private String difficulty;
    private int noOfQuestions;
    private Instant timestamp;

    // Default constructor for serialization/deserialization
    public QuizEvent() {}

    // Full constructor
    public QuizEvent(String eventType, int quizId, String title, String difficulty, int noOfQuestions) {
        this.eventType = eventType;
        this.quizId = quizId;
        this.title = title;
        this.difficulty = difficulty;
        this.noOfQuestions = noOfQuestions;
        this.timestamp = Instant.now();
    }

    // Minimal constructor
    public QuizEvent(String eventType, int quizId) {
        this.eventType = eventType;
        this.quizId = quizId;
        this.timestamp = Instant.now();
    }

    // ===== Getters and Setters =====
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getNoOfQuestions() { return noOfQuestions; }
    public void setNoOfQuestions(int noOfQuestions) { this.noOfQuestions = noOfQuestions; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "QuizEvent{" +
                "eventType='" + eventType + '\'' +
                ", quizId=" + quizId +
                ", title='" + title + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", noOfQuestions=" + noOfQuestions +
                ", timestamp=" + timestamp +
                '}';
    }
}
