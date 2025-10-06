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

    public ProfileDto createProfile(ProfileDto profileDto) {
        if (repo.existsByClerkId(profileDto.getClerkId())) {
            return updateProfile(profileDto);
        }

        Profile profile = new Profile();
        profile.setClerkId(profileDto.getClerkId());
        profile.setFirstName(profileDto.getFirstName());
        profile.setLastName(profileDto.getLastName());
        profile.setEmail(profileDto.getEmail());
        profile.setPhotoUrl(profileDto.getPhotoUrl());
        profile.setCreatedAt(Instant.now());

        // Set plan with fallback
        if (profileDto.getPlan() != null && !profileDto.getPlan().isEmpty()) {
            try {
                profile.setPlan(Profile.Plan.valueOf(profileDto.getPlan().toUpperCase()));
            } catch (IllegalArgumentException e) {
                profile.setPlan(DEFAULT_USER_PLAN);
            }
        } else {
            profile.setPlan(DEFAULT_USER_PLAN);
        }

        // Points only, no role
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

            if (profileDto.getPlan() != null) {
                try {
                    existingProfile.setPlan(Profile.Plan.valueOf(profileDto.getPlan().toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            repo.save(existingProfile);
            return mapToDto(existingProfile);
        }
        return null;
    }

    public boolean existsByClerkId(String clerkId) {
        return repo.existsByClerkId(clerkId);
    }

    public void deleteProfile(String clerkId) {
        Profile existingProfile = repo.findByClerkId(clerkId);
        if (existingProfile != null) repo.delete(existingProfile);
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

    Profile profile = repo.findByClerkId(clerkId);

    if (profile == null) {
        // Retrieve email and other info from auth principal (assuming a UserDetails-like object)
        String email = ""; // fetch from auth principal
        String firstName = ""; // fetch from auth principal
        String lastName = ""; // fetch from auth principal
        String photoUrl = ""; // fetch from auth principal if available

        if (email == null || email.isEmpty()) {
            throw new IllegalStateException("Cannot create profile: email is missing");
        }

        Profile newProfile = new Profile();
        newProfile.setClerkId(clerkId);
        newProfile.setEmail(email);
        newProfile.setFirstName(firstName);
        newProfile.setLastName(lastName);
        newProfile.setPhotoUrl(photoUrl);
        newProfile.setPoints(DEFAULT_USER_POINTS);
        newProfile.setPlan(DEFAULT_USER_PLAN);
        newProfile.setCreatedAt(Instant.now());

        profile = repo.save(newProfile);
    }

    return profile;
}



    public int getQuizzesCreatedThisMonth(int profileId) {
        Profile profile = repo.findByClerkId(getCurrentProfile().getClerkId());
        if (profile == null) return 0;
        List<Quiz> quizzes = quizDao.findByCreatedBy(profile.getClerkId());
        YearMonth currentMonth = YearMonth.now();
        return (int) quizzes.stream()
                .filter(q -> YearMonth.from(q.getCreatedAt()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()).equals(currentMonth))
                .count();
    }

    public void incrementQuizCreatedCount(Profile profile) {
        profile.setPoints(profile.getPoints() - 1);
        repo.save(profile);
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
