package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.ProfileDao;
import com.web.CertiQuest.dao.QuizDao;
import com.web.CertiQuest.dto.ProfileDto;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Service
public class ProfileService {

    @Autowired
    private ProfileDao repo;

    @Autowired
    private QuizDao quizDao;

    private static final Profile.Plan DEFAULT_USER_PLAN = Profile.Plan.FREE;
    private static final int DEFAULT_USER_POINTS = 10;
    
    String currentClerkId;

    public ProfileDto createProfile(ProfileDto profileDto) {
        if (repo.existsByClerkId(profileDto.getClerkId())) {
            return updateProfile(profileDto);
        }
        
        currentClerkId = profileDto.getClerkId();

        Profile profile = new Profile();
        profile.setClerkId(profileDto.getClerkId());
        profile.setFirstName(profileDto.getFirstName());
        profile.setLastName(profileDto.getLastName());
        profile.setEmail(profileDto.getEmail());
        profile.setPhotoUrl(profileDto.getPhotoUrl());
        profile.setCreatedAt(Instant.now());
        profile.setPlan(parsePlan(profileDto.getPlan()));
        profile.setPoints(DEFAULT_USER_POINTS);

        profile = repo.save(profile);
        return mapToDto(profile);
    }

    public ProfileDto updateProfile(ProfileDto profileDto) {
        Profile existingProfile = repo.findByClerkId(profileDto.getClerkId());
        if (existingProfile != null) {
            if (profileDto.getEmail() != null) existingProfile.setEmail(profileDto.getEmail());
            if (profileDto.getFirstName() != null) existingProfile.setFirstName(profileDto.getFirstName());
            if (profileDto.getLastName() != null) existingProfile.setLastName(profileDto.getLastName());
            if (profileDto.getPhotoUrl() != null) existingProfile.setPhotoUrl(profileDto.getPhotoUrl());
            if (profileDto.getPlan() != null) existingProfile.setPlan(parsePlan(profileDto.getPlan()));

            repo.save(existingProfile);
            return mapToDto(existingProfile);
        }
        return null;
    }

    public boolean existsByClerkId(String clerkId) {
        return repo.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId) {
        Profile profile = repo.findByClerkId(clerkId);
        if (profile != null) repo.delete(profile);
    }

    public Profile getCurrentProfile() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        String clerkId = auth.getName();
        if (clerkId == null || clerkId.isEmpty()) {
            throw new UsernameNotFoundException("User identifier (clerkId) is missing");
        }

        Profile profile = repo.findByClerkId(currentClerkId);
        if (profile == null) {
            profile = new Profile();
            profile.setClerkId(clerkId);
            // TODO: populate mandatory fields like email, firstName, lastName
            profile.setEmail("Anonymous");  // Must not be null!
            profile.setFirstName("Anony");
            profile.setLastName("mous");
            profile.setPoints(DEFAULT_USER_POINTS);
            profile.setPlan(DEFAULT_USER_PLAN);
            profile.setCreatedAt(Instant.now());
            profile = repo.save(profile);
        }
        return profile;
    }

    public int getQuizzesCreatedThisMonth(int id) {
        Profile profile = getCurrentProfile();
        List<Quiz> quizzes = quizDao.findByCreatedBy(profile.getClerkId());
        YearMonth currentMonth = YearMonth.now();

        return (int) quizzes.stream()
                .filter(q -> YearMonth.from(q.getCreatedAt()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate())
                        .equals(currentMonth))
                .count();
    }

    public void incrementQuizCreatedCount(Profile profile) {
        if (profile.getPoints() <= 0) {
            throw new IllegalStateException("Insufficient points to create a quiz");
        }
        profile.setPoints(profile.getPoints() - 1);
        repo.save(profile);
    }

    private Profile.Plan parsePlan(String planStr) {
        if (planStr != null && !planStr.isEmpty()) {
            try {
                return Profile.Plan.valueOf(planStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return DEFAULT_USER_PLAN;
    }

    private ProfileDto mapToDto(Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setClerkId(profile.getClerkId());
        dto.setEmail(profile.getEmail());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhotoUrl(profile.getPhotoUrl());
        dto.setPoints(profile.getPoints());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setPlan(profile.getPlan() != null ? profile.getPlan().name() : DEFAULT_USER_PLAN.name());
        return dto;
    }
}
