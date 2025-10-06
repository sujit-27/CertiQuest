package com.web.CertiQuest.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.resend.services.emails.model.Email;
import com.web.CertiQuest.dao.ProfileDao;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ProfileDao profileDao;
    private final Resend resend;

    public EmailService(ProfileDao profileDao, @Value("${resend.api.key}") String apiKey) {
        this.profileDao = profileDao;
        this.resend = new Resend(apiKey);
    }

    // ---------------- Send new quiz notification ----------------
    public void sendQuizCreatedMailToAllExceptCreator(Quiz quiz, String creatorClerkId) {
        List<Profile> profiles = profileDao.findAll();
        Profile creatorProfile = profileDao.findByClerkId(creatorClerkId);
        if (creatorProfile == null || creatorProfile.getEmail() == null) {
            logger.error("Creator profile or email not found for clerkId: {}", creatorClerkId);
            return;
        }
        String creatorEmail = creatorProfile.getEmail();
        String subject = "CertiQuest: 🧠 New Quiz Available – " + quiz.getTitle();
        String body = buildQuizCreatedEmailBody(quiz);

        profiles.stream()
                .map(Profile::getEmail)
                .filter(email -> email != null && !email.equalsIgnoreCase(creatorEmail))
                .forEach(email -> sendEmail(email, subject, body));
    }

    // ---------------- Send updated quiz notification ----------------
    public void sendQuizUpdatedMailToAllExceptCreator(Quiz quiz, String creatorClerkId) {
        List<Profile> profiles = profileDao.findAll();
        Profile creatorProfile = profileDao.findByClerkId(creatorClerkId);
        if (creatorProfile == null || creatorProfile.getEmail() == null) {
            logger.error("Creator profile or email not found for clerkId: {}", creatorClerkId);
            return;
        }
        String creatorEmail = creatorProfile.getEmail();
        String subject = "CertiQuest: 🔄 Quiz Updated – " + quiz.getTitle();
        String body = buildQuizUpdatedEmailBody(quiz);

        profiles.stream()
                .map(Profile::getEmail)
                .filter(email -> email != null && !email.equalsIgnoreCase(creatorEmail))
                .forEach(email -> sendEmail(email, subject, body));
    }

    // ---------------- Notify creator about participant ----------------
    public void sendParticipantJoinedMailToCreator(Quiz quiz, String participantClerkId, String creatorClerkId) {
        Profile creatorProfile = profileDao.findByClerkId(creatorClerkId);
        Profile participantProfile = profileDao.findByClerkId(participantClerkId);

        if (creatorProfile == null || creatorProfile.getEmail() == null) {
            logger.error("Creator profile or email not found for clerkId: {}", creatorClerkId);
            return;
        }
        if (participantProfile == null) {
            logger.error("Participant profile not found for clerkId: {}", participantClerkId);
            return;
        }

        String creatorEmail = creatorProfile.getEmail();
        String participantName = participantProfile.getFirstName() + " " + participantProfile.getLastName();

        String subject = "CertiQuest: 👤 New Participant Joined – " + quiz.getTitle();
        String body = "Hello!\n\n" +
                "A new participant has joined your quiz \"" + quiz.getTitle() + "\" on CertiQuest.\n\n" +
                "📌 Participant Details:\n" +
                "Name       : " + participantName + "\n" +
                "Quiz Title : " + quiz.getTitle() + "\n" +
                "Category   : " + quiz.getCategory() + "\n" +
                "Difficulty : " + quiz.getDifficulty() + "\n\n" +
                "You can monitor participant progress and results in your dashboard.\n\n" +
                "— The CertiQuest Team";

        sendEmail(creatorEmail, subject, body);
    }

    // ---------------- Core email sender using Resend ----------------
    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            // Create an email object using the Resend SDK
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")  // Use your verified Resend email
                    .to("sujitshaw029@gmail.com")
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            // Send the email using the Resend SDK
            CreateEmailResponse response = resend.emails().send(params);

            // Log the response ID
            logger.info("✅ Mail sent successfully to {}: {}", to, response.getId());
        } catch (ResendException e) {
            logger.error("❌ Error sending mail to {}: {}", to, e.getMessage());
        }
    }


    // ---------------- Email templates ----------------
    private String buildQuizCreatedEmailBody(Quiz quiz) {
        return "Hello!\n\n" +
                "A new quiz has just been created on CertiQuest, and we wanted you to be among the first to know!\n\n" +
                "📌 Quiz Details:\n" +
                "Title       : " + quiz.getTitle() + "\n" +
                "Category    : " + quiz.getCategory() + "\n" +
                "Difficulty  : " + quiz.getDifficulty() + "\n" +
                "Questions   : " + quiz.getNoOfQuestions() + "\n\n" +
                "Participate now to test your knowledge and earn certificates!\n\n" +
                "🔗 Access the quiz here: [Insert Quiz URL]\n\n" +
                "Happy quizzing! 🏆\n\n" +
                "— The CertiQuest Team";
    }

    private String buildQuizUpdatedEmailBody(Quiz quiz) {
        return "Hello!\n\n" +
                "The quiz \"" + quiz.getTitle() + "\" has been updated on CertiQuest.\n\n" +
                "📌 Updated Quiz Details:\n" +
                "Title       : " + quiz.getTitle() + "\n" +
                "Category    : " + quiz.getCategory() + "\n" +
                "Difficulty  : " + quiz.getDifficulty() + "\n" +
                "Questions   : " + quiz.getNoOfQuestions() + "\n\n" +
                "Check out the latest version and try it out!\n\n" +
                "🔗 Access the quiz here: [Insert Quiz URL]\n\n" +
                "— The CertiQuest Team";
    }
}
