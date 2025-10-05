package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.ProfileDao;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ProfileDao profileDao;

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

        try {
            sendEmail(creatorEmail, subject, body);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", creatorEmail, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            logger.info("✅ Mail sent to {}", to);
        } catch (Exception e) {
            logger.error("❌ Error sending mail to {}: {}", to, e.getMessage());
        }
    }

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
