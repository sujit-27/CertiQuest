package com.web.CertiQuest.service;

import com.web.CertiQuest.model.QuizEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminQuizEventProducer {

    private static final String QUIZ_TOPIC = "quiz_admin";

    @Autowired
    private KafkaTemplate<String, QuizEvent> kafkaTemplate;

    public void sendQuizEvent(QuizEvent event) {
        kafkaTemplate.send(QUIZ_TOPIC, event);
        System.out.println("✅ Sent Quiz Event: " + event);
    }
}
