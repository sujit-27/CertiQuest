package com.web.CertiQuest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.CertiQuest.dto.ProfileDto;
import com.web.CertiQuest.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1.0/webhooks")
@RequiredArgsConstructor
public class ClerkWebhookController {

    @Value("${clerk.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private ProfileService profileService;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleClerkWebhook(@RequestHeader("svix-id") String svixId,
                                                @RequestHeader("svix-timestamp") String svixTimestamp,
                                                @RequestHeader("svix-signature") String svixSignature,
                                                @RequestBody String payload) {
        System.out.println("✅ Webhook Received from Clerk");

        try {
            boolean isValid = verifyWebhookSignature(svixId, svixTimestamp, svixSignature, payload);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventType = rootNode.path("type").asText();

            switch (eventType) {
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;
                default:
                    System.out.println("ℹ️ Unhandled Clerk event type: " + eventType);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private void handleUserDeleted(JsonNode data) {
        String clerkId = data.path("id").asText();
        System.out.println("🗑 Deleting profile for clerkId: " + clerkId);
        profileService.deleteProfile(clerkId);
    }

    private void handleUserUpdated(JsonNode data) {
        ProfileDto updatedProfile = extractProfileDtoFromClerkData(data);
        updatedProfile = profileService.updateProfile(updatedProfile);
        if (updatedProfile == null) {
            // If user not found, create a new one
            handleUserCreated(data);
        }
    }

    private void handleUserCreated(JsonNode data) {
        ProfileDto newProfile = extractProfileDtoFromClerkData(data);
        ProfileDto savedProfile = profileService.createProfile(newProfile);
        System.out.println("🎉 New Profile Created: " + savedProfile);
    }

    /**
     * Extracts ProfileDto from Clerk's webhook payload.
     * Pulls out email, name, photo, and public_metadata (role & plan).
     */
    private ProfileDto extractProfileDtoFromClerkData(JsonNode data) {
        String clerkId = data.path("id").asText();

        String email = "";
        JsonNode emailAddresses = data.path("email_addresses");
        if (emailAddresses.isArray() && !emailAddresses.isEmpty()) {
            email = emailAddresses.get(0).path("email_address").asText();
        }

        String firstName = data.path("first_name").asText("");
        String lastName = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        // 🔑 Extract custom fields from public_metadata
        JsonNode publicMetadata = data.path("public_metadata");
        String role = publicMetadata.path("role").asText("").toUpperCase(); // e.g., "ADMIN", "USER"
        String plan = publicMetadata.path("plan").asText("").toUpperCase(); // e.g., "FREE", "PRO"

        System.out.println("📩 Clerk Webhook Data → Role: " + role + ", Plan: " + plan);

        ProfileDto dto = new ProfileDto();
        dto.setClerkId(clerkId);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setPhotoUrl(photoUrl);

        if (!plan.isEmpty()) dto.setPlan(plan);

        return dto;
    }

    private boolean verifyWebhookSignature(String svixId, String svixTimestamp, String svixSignature, String payload) {
        // ✅ Stub for now — you can add actual verification using Clerk's Java SDK later
        return true;
    }

}
