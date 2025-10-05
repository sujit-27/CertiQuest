package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.UserPointsDao;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.UserPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserPointsService {

    private static final Logger logger = LoggerFactory.getLogger(UserPointsService.class);

    @Autowired
    private UserPointsDao repo;
    @Autowired
    private ProfileService profileService;

    // Default values
    private static final int DEFAULT_POINTS = 10;
    private static final String DEFAULT_PLAN = "FREE";


    private UserPoints createInitialPoints(String clerkId) {
        UserPoints userPoints = new UserPoints();
        userPoints.setClerkId(clerkId);
        userPoints.setPoints(DEFAULT_POINTS);
        userPoints.setPlan(DEFAULT_PLAN);
        logger.info("Created initial UserPoints for clerkId {}: {}", clerkId, userPoints);
        return repo.save(userPoints);
    }

    public UserPoints getUserPoints(String clerkId) {
        return repo.findByClerkId(clerkId)
                .orElseGet(() -> createInitialPoints(clerkId));
    }

    public Optional<UserPoints> getUserPointsForCurrentUser() {
        var profile = profileService.getCurrentProfile();
        if (profile == null) {
            logger.warn("Attempted to get points for unauthenticated user");
            return Optional.empty();
        }
        return Optional.of(getUserPoints(profile.getClerkId()));
    }

    public boolean hasEnoughPoints(int requiredPoints) {
        return getUserPointsForCurrentUser()
                .map(up -> up.getPoints() >= requiredPoints)
                .orElse(false);
    }

    public Optional<UserPoints> consumePoints(int requiredPoints) {
        Profile profile = profileService.getCurrentProfile();
        if (profile == null) {
            logger.warn("Cannot consume points for unauthenticated user");
            return Optional.empty();
        }

        String clerkId = profile.getClerkId();
        Optional<UserPoints> optionalUserPoints = repo.findByClerkId(clerkId);

        if (optionalUserPoints.isEmpty()) {
            logger.warn("No UserPoints found for clerkId {} to consume", clerkId);
            return Optional.empty();
        }

        UserPoints userPoints = optionalUserPoints.get();

        if (userPoints.getPoints() < requiredPoints) {
            logger.info("User {} has insufficient points (needed {}, has {})",
                    clerkId, requiredPoints, userPoints.getPoints());
            return Optional.empty();
        }

        // Deduct required points
        userPoints.setPoints(userPoints.getPoints() - requiredPoints);
        UserPoints updatedUserPoints = repo.save(userPoints);

        logger.info("Consumed {} points for user {}, new balance: {}",
                requiredPoints, clerkId, updatedUserPoints.getPoints());

        return Optional.of(updatedUserPoints);
    }

    public void addPoints(String clerkId, int pointsToAdd, String plan) {
        UserPoints userPoints = repo.findByClerkId(clerkId)
                .orElseGet(() -> createInitialPoints(clerkId));

        userPoints.setPoints(userPoints.getPoints() + pointsToAdd);

        if (plan != null && !plan.isBlank()) {
            userPoints.setPlan(plan);
        }

        logger.info("Added {} points to user {}, new balance: {}, plan set to {}",
                pointsToAdd, clerkId, userPoints.getPoints(), userPoints.getPlan());

        repo.save(userPoints);
    }
}
