package com.web.CertiQuest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_points")
public class UserPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String clerkId;
    private int points;
    private String plan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClerkId() {
        return clerkId;
    }

    public void setClerkId(String clerkId) {
        this.clerkId = clerkId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    @Override
    public String toString() {
        return "UserPoints{" +
                "id=" + id +
                ", clerkId='" + clerkId + '\'' +
                ", points=" + points +
                ", plan='" + plan + '\'' +
                '}';
    }
}

