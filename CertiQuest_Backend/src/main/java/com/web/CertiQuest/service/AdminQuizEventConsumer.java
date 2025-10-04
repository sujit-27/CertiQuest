package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.ProfileDao;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.QuizEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminQuizEventConsumer {

    private final ProfileDao userDao;
    private final JavaMailSender javaMailSender;

    @Autowired
    public AdminQuizEventConsumer(ProfileDao userDao, JavaMailSender javaMailSender) {
        this.userDao = userDao;
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = "quiz_admin", groupId = "certiquest-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(QuizEvent event) {
        try {
            List<Profile> users = userDao.findAll(); // fetch all users with emails

            for (Profile user : users) {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(user.getEmail());
                mail.setSubject("CertiQuest: New Quiz Available - " + event.getTitle());
                mail.setText("Hello,\n\n" +
                        "A new quiz has just been created on CertiQuest!\n\n" +
                        "Title: " + event.getTitle() + "\n" +
                        "Difficulty Level: " + event.getDifficulty() + "\n" +
                        "Number of Questions: " + event.getNoOfQuestions() + "\n\n" +
                        "Log in to your account to participate and test your knowledge.\n\n" +
                        "Happy Quizzing!\n" +
                        "The CertiQuest Team");

                javaMailSender.send(mail);
            }
            System.out.println("✅ Email notifications sent for event: " + event);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email notifications: " + e.getMessage());
        }
    }
}
