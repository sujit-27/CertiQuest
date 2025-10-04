package com.web.CertiQuest.controller;

import com.web.CertiQuest.model.QuizQuestion;
import com.web.CertiQuest.service.QuizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizQuestionController {

    @Autowired
    private QuizQuestionService quizQuestionService;

    @PostMapping("/questions")
    public ResponseEntity<?> getQuestions (@RequestParam String category, @RequestParam String difficulty, @RequestParam int noOfQuestions){
        List<QuizQuestion> questions = quizQuestionService.generateQuestions(category,difficulty,noOfQuestions);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuizQuestion>> getQuiz(
            @RequestParam String category,
            @RequestParam String difficulty,
            @RequestParam int noOfQuestions) {

        List<QuizQuestion> questions = quizQuestionService.getOrCreateQuiz(category, difficulty, noOfQuestions);
        return ResponseEntity.ok(questions);
    }

}
