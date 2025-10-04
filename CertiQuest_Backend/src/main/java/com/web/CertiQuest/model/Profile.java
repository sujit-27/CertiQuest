package com.web.CertiQuest.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String clerkId;

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String lastName;
    private String photoUrl;
    private Instant createdAt;
    private int points;

    @Enumerated(EnumType.STRING)
    private Plan plan;

    public enum Plan {
        FREE,
        PRO,
        ADMIN
    }

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClerkId() { return clerkId; }
    public void setClerkId(String clerkId) { this.clerkId = clerkId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", clerkId='" + clerkId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", createdAt=" + createdAt +
                ", points=" + points +
                ", plan=" + plan +
                '}';
    }
}
