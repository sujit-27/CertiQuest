package com.web.CertiQuest.controller;

import com.web.CertiQuest.dto.QuizDto;
import com.web.CertiQuest.dto.QuizSubmissionDto;
import com.web.CertiQuest.model.Quiz;
import com.web.CertiQuest.model.QuizEvent;
import com.web.CertiQuest.model.QuizResult;
import com.web.CertiQuest.service.AdminQuizEventProducer;
import com.web.CertiQuest.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private AdminQuizEventProducer adminQuizEventProducer;

    // ===== Create Quiz =====
    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizDto request) {
        try {
            Quiz quiz = quizService.createQuiz(
                    request.getTitle(),
                    request.getCategory(),
                    request.getDifficulty(),
                    request.getNoOfQuestions(),
                    request.getCreatedBy()
            );

            try {
                adminQuizEventProducer.sendQuizEvent(
                        new QuizEvent(
                                "CREATED",
                                quiz.getId(),
                                quiz.getTitle(),
                                quiz.getDifficulty(),
                                quiz.getNoOfQuestions()
                        )
                );
            } catch (Exception e) {
                System.err.println("Kafka event sending failed: " + e.getMessage());
            }


            return ResponseEntity.ok(quiz);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===== Create Quiz from PDF =====
    @PostMapping("/create-quiz-pdf")
    public ResponseEntity<?> uploadQuizPdf(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam("createdBy") String createdBy
    ) {
        try {
            Quiz savedQuiz = quizService.createQuizFromPdf(pdfFile, title, category, difficulty, createdBy);

            try {
                adminQuizEventProducer.sendQuizEvent(
                        new QuizEvent(
                                "CREATED",
                                savedQuiz.getId(),
                                savedQuiz.getTitle(),
                                savedQuiz.getDifficulty(),
                                savedQuiz.getNoOfQuestions()
                        )
                );
            } catch (Exception e) {
                System.err.println("Kafka event sending failed: " + e.getMessage());
            }
            return ResponseEntity.ok(savedQuiz);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===== Update Quiz =====
    @PutMapping("/update")
    public ResponseEntity<?> updateQuiz(
            @RequestParam int id,
            @RequestParam String title,
            @RequestParam String difficulty,
            @RequestParam int noOfQuestions
    ) {
        Quiz updatedQuiz = quizService.updateQuiz(id, title, difficulty, noOfQuestions);

        try {
            adminQuizEventProducer.sendQuizEvent(
                    new QuizEvent(
                            "UPDATED",
                            updatedQuiz.getId(),
                            updatedQuiz.getTitle(),
                            updatedQuiz.getDifficulty(),
                            updatedQuiz.getNoOfQuestions()
                    )
            );
        } catch (Exception e) {
            // Log error, but do not fail quiz update
            System.err.println("Kafka event sending failed: " + e.getMessage());
        }

        return ResponseEntity.ok(updatedQuiz);
    }

    // ===== Get Quiz by ID =====
    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable int quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quiz);
    }

    // ===== Get All Quizzes =====
    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    // ===== Join Quiz =====
    @PostMapping("/{quizId}/join")
    public ResponseEntity<?> joinQuiz(@PathVariable int quizId, @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        quizService.addParticipant(quizId, userId);
        return ResponseEntity.ok(Map.of("message", "Joined quiz successfully"));
    }

    // ===== Submit Quiz =====
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable int quizId,
            @RequestBody QuizSubmissionDto submission,
            @RequestParam String userId
    ) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (!quiz.getParticipants().contains(userId) && !quiz.getCreatedBy().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You are not allowed to submit this quiz"));
        }

        QuizResult savedResult = quizService.evaluateAndSaveResult(submission, userId);

        return ResponseEntity.ok(Map.of(
                "score", savedResult.getScore(),
                "total", savedResult.getTotalQuestions()
        ));
    }

    // ===== Get User Results =====
    @GetMapping("/results/{userId}")
    public ResponseEntity<?> getUserResults(@PathVariable String userId) {
        return ResponseEntity.ok("History for " + userId);
    }

    // ===== Delete Quiz =====
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteQuiz(@RequestParam int id) {
        try {
            quizService.deleteQuiz(id);

            return ResponseEntity.ok("Quiz with ID " + id + " has been deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete quiz with ID " + id + ". Reason: " + e.getMessage());
        }
    }
}
