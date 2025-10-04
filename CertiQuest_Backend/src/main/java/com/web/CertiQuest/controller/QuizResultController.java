package com.web.CertiQuest.controller;

import com.web.CertiQuest.dto.QuizResultDto;
import com.web.CertiQuest.service.QuizResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizResultController {

    @Autowired
    private QuizResultService quizResultService;

    @GetMapping("/results/{userId}")
    public ResponseEntity<List<QuizResultDto>> getUserResults(@PathVariable String userId) {
        List<QuizResultDto> results = quizResultService.getUserResults(userId);
        return ResponseEntity.ok(results);
    }
}

