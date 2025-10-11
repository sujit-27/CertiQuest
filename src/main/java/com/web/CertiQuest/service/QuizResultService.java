package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.QuizDao;
import com.web.CertiQuest.dao.QuizResultDao;
import com.web.CertiQuest.dto.QuizResultDto;
import com.web.CertiQuest.model.QuizResult;
import com.web.CertiQuest.model.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultDao quizResultRepository;

    @Autowired
    private QuizDao quizRepository;

    @Autowired
    private CertificateService certificateService;


    public List<QuizResultDto> getUserResults(String userId) {
        List<QuizResult> results = quizResultRepository.findByUserId(userId);
        return results.stream().map(result -> {
            Quiz quiz = quizRepository.findById(result.getQuizId()).orElse(null);
            QuizResultDto dto = new QuizResultDto();
            dto.setQuizId(result.getQuizId());
            dto.setQuizTitle(quiz != null ? quiz.getTitle() : "Unknown Quiz");
            dto.setScore(result.getScore());
            dto.setTotal(result.getTotalQuestions());
            dto.setSubmittedAt(result.getAttemptedAt());
            dto.setParticipants(quiz != null ? quiz.getParticipants() : Collections.emptyList());
            return dto;
        }).collect(Collectors.toList());
    }

    public void saveQuizResultAndGenerateCertificate(String userId, String userName, int quizId,
                                                     int score, int totalQuestions, String difficulty) throws Exception {


        QuizResult result = new QuizResult();
        result.setUserId(userId);
        result.setQuizId(quizId);
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setAttemptedAt(Instant.from(LocalDate.now()));
        quizResultRepository.save(result);

        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        String quizTitle = (quiz != null) ? quiz.getTitle() : "Quiz";

        double percentage = (score * 100.0) / totalQuestions;

        // Check if certificate criteria are met
        if (totalQuestions >= 10 &&
                percentage >= 60 &&
                (difficulty.equalsIgnoreCase("medium") || difficulty.equalsIgnoreCase("hard"))) {

            certificateService.generateCertificate(userId, userName, quizTitle, score, totalQuestions, difficulty, quizId);
        }
    }
}
