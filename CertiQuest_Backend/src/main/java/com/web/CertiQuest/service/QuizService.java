package com.web.CertiQuest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.web.CertiQuest.dao.QuizDao;
import com.web.CertiQuest.dao.QuizQuestionDao;
import com.web.CertiQuest.dao.QuizResultDao;
import com.web.CertiQuest.dao.UserPointsDao;
import com.web.CertiQuest.dto.QuizSubmissionDto;
import com.web.CertiQuest.model.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizDao quizDao;
    @Autowired
    private QuizQuestionDao quizQuestionDao;
    @Autowired
    private QuizQuestionService quizQuestionService;
    @Autowired
    private QuizResultDao quizResultDao;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private UserPointsService userPointsService;
    @Autowired
    private UserPointsDao userPointsDao;
    @Autowired
    private EmailService emailService;

    /**
     * Create a new quiz and deduct points
     */
    @Transactional
    public Quiz createQuiz(String title, String category, String difficulty, int noOfQuestions, String createdBy) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to create quiz"));

        if (noOfQuestions <= 0) {
            throw new IllegalArgumentException("Number of questions must be greater than zero.");
        }

        Profile creator = profileService.getCurrentProfile();
        validateAdminPlanForQuizCreation(creator, difficulty, noOfQuestions);

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setDifficulty(difficulty);
        quiz.setNoOfQuestions(noOfQuestions);
        quiz.setCreatedAt(Instant.now());
        quiz.setCreatedBy(createdBy);
        quiz.setExpiryDate(LocalDate.now().plusDays(7));

        // Get questions (may be existing pool items or newly generated)
        List<QuizQuestion> questions = quizQuestionService.getOrCreateQuiz(category, difficulty, noOfQuestions);

        // Attach questions to the quiz without replacing the collection reference
        // Use managed entity when question already exists in DB
        for (QuizQuestion q : questions) {
            if (q.getId() > 0) {
                QuizQuestion managed = quizQuestionDao.findById(q.getId()).orElse(q);
                quiz.addQuestion(managed);
            } else {
                quiz.addQuestion(q); // new entity, cascade will persist it when saving quiz
            }
        }

        // Save quiz (CascadeType.ALL on questions will persist new questions and update managed ones)

        Quiz savedQuiz = quizDao.save(quiz);
        System.out.println(savedQuiz);

        profileService.incrementQuizCreatedCount(creator);
        emailService.sendQuizCreatedMailToAllExceptCreator(savedQuiz, createdBy);
        return savedQuiz;
    }

    /**
     * Create a quiz from a PDF and deduct points
     */
    @Transactional
    public Quiz createQuizFromPdf(MultipartFile pdfFile, String title,
                                  String category, String difficulty, String createdBy) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to create quiz"));

        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required.");
        }
        if (!"application/pdf".equalsIgnoreCase(pdfFile.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }

        String pdfText = "";
        try (InputStream pdfInputStream = pdfFile.getInputStream();
             PDDocument document = PDDocument.load(pdfInputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(document);

            if (pdfText.trim().isEmpty()) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                StringBuilder ocrText = new StringBuilder();
                ITesseract tesseract = new Tesseract();
                File tessDataFolder = new File(getClass().getClassLoader().getResource("tessdata").getFile());
                tesseract.setDatapath(tessDataFolder.getAbsolutePath());
                tesseract.setLanguage("eng");

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                    String result = tesseract.doOCR(image);
                    ocrText.append(result).append("\n");
                }
                pdfText = ocrText.toString();
                if (pdfText.trim().isEmpty()) {
                    throw new RuntimeException("Cannot extract questions: PDF contains no text even after OCR.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF file", e);
        }

        // Use improved extraction
        List<QuizQuestion> questions = quizQuestionService.extractQuestionsFromText(
                pdfText,
                category != null ? category : "General",
                difficulty != null ? difficulty : "Medium"
        );

        if (questions.isEmpty()) {
            throw new RuntimeException("No quiz questions could be extracted from PDF.");
        }

        Profile creator = profileService.getCurrentProfile();
        validateAdminPlanForQuizCreation(creator, difficulty, questions.size());

        Quiz quiz = new Quiz();
        quiz.setTitle(title != null && !title.isEmpty() ? title : "User uploaded quiz: " + pdfFile.getOriginalFilename());
        quiz.setCategory(category != null ? category : "General");
        quiz.setDifficulty(difficulty != null ? difficulty : "Medium");
        quiz.setCreatedBy(createdBy);
        quiz.setNoOfQuestions(questions.size());
        quiz.setCreatedAt(Instant.now());
        quiz.setExpiryDate(LocalDate.now().plusDays(7));

        // Use addQuestion to set both sides and persist via cascade
        for (QuizQuestion q : questions) {
            quiz.addQuestion(q);
        }

        Quiz savedQuiz = quizDao.save(quiz);

        profileService.incrementQuizCreatedCount(creator);
        emailService.sendQuizCreatedMailToAllExceptCreator(savedQuiz, createdBy);

        return savedQuiz;
    }


    /**
     * Evaluate submission and deduct 1 point for attending quiz
     */
    public QuizResult evaluateAndSaveResult(QuizSubmissionDto submission, String userId) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to attend quiz"));

        if (submission.getQuizId() <= 0) {
            throw new RuntimeException("Invalid quizId: " + submission.getQuizId());
        }

        int score = calculateScore(submission);

        QuizResult result = new QuizResult();
        result.setQuizId(submission.getQuizId());
        result.setUserId(userId);
        result.setScore(score);
        result.setTotalQuestions(submission.getAnswers().size());
        quizResultDao.save(result);

        Quiz quiz = quizDao.findById(submission.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + submission.getQuizId()));

        if (!quiz.getParticipants().contains(userId)) {
            quiz.getParticipants().add(userId);
            quizDao.save(quiz);
        }

        return result;
    }

    private void validateAdminPlanForQuizCreation(Profile user, String difficulty, int noOfQuestions) {
        String userId = user.getClerkId();
        Optional<UserPoints> userPoints = userPointsDao.findByClerkId(userId);
        String plan = userPoints.get().getPlan();
        switch (plan) {
            case "BASIC" -> {
                if (noOfQuestions > 10) {
                    throw new RuntimeException("Free plan users can only create quizzes with up to 10 questions.");
                }
                if ("HARD".equalsIgnoreCase(difficulty)) {
                    throw new RuntimeException("Free plan users cannot create HARD difficulty quizzes.");
                }
                int createdThisMonth = profileService.getQuizzesCreatedThisMonth(user.getId());
                if (createdThisMonth >= 5) {
                    throw new RuntimeException("Free plan users can only create 5 quizzes per month.");
                }
            }
            case "PREMIUM", "ULTIMATE" -> {
                // No restrictions
            }
        }
    }

    public Quiz getQuizById(int quizId) {
        return quizDao.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
    }

    public List<Quiz> getAllQuizzes() {
        return quizDao.findAll();
    }

    public int calculateScore(QuizSubmissionDto submission) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(submission);
            System.out.println("Quiz Submission: \n" + json);
        } catch (Exception e) {
            System.out.println("Error serializing submission: " + e.getMessage());
        }

        AtomicInteger score = new AtomicInteger();

        for (QuizSubmissionDto.UserAnswer ans : submission.getAnswers()) {
            System.out.println("Answer: QuestionId=" + ans.getQuestionId() + ", SelectedAnswer=" + ans.getSelectedAnswer());

            quizQuestionDao.findById(ans.getQuestionId())
                    .ifPresent(question -> {
                        System.out.println("Question: " + question.getQuestion());
                        System.out.println("Correct answer: " + question.getCorrectAnswer());

                        if (question.getCorrectAnswer().equalsIgnoreCase(ans.getSelectedAnswer())) {
                            System.out.println("Answer correct.");
                            score.getAndIncrement();
                        } else {
                            System.out.println("Answer incorrect.");
                        }
                    });
        }

        System.out.println("Final score: " + score.get());

        return score.get();
    }


    @Transactional
    public Quiz updateQuiz(int id, String title, String difficulty, int noOfQuestions) {
        Quiz quiz = quizDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz with id " + id + " not found"));

        quiz.setTitle(title);

        boolean difficultyChanged = !Objects.equals(quiz.getDifficulty(), difficulty);
        boolean questionCountChanged = !Objects.equals(quiz.getNoOfQuestions(), noOfQuestions);

        if (difficultyChanged || questionCountChanged) {
            quiz.setDifficulty(difficulty);
            quiz.setNoOfQuestions(noOfQuestions);

            List<QuizQuestion> newQuestions = quizQuestionService.getOrCreateQuiz(
                    quiz.getCategory(), difficulty, noOfQuestions
            );

            // If Cohere returned all-new questions (ids == 0), remove all existing questions first.
            boolean allNew = newQuestions.stream().allMatch(q -> q.getId() <= 0);

            if (allNew) {
                // remove all existing questions via removeQuestion to keep JPA consistency
                for (QuizQuestion q : new ArrayList<>(quiz.getQuestions())) {
                    quiz.removeQuestion(q);
                }
            } else {
                // Do id-based diff: remove existing questions that are not in newQuestions (by id)
                Set<Integer> newIds = newQuestions.stream()
                        .filter(q -> q.getId() > 0)
                        .map(QuizQuestion::getId)
                        .collect(Collectors.toSet());

                for (QuizQuestion existing : new ArrayList<>(quiz.getQuestions())) {
                    if (existing.getId() > 0 && !newIds.contains(existing.getId())) {
                        quiz.removeQuestion(existing);
                    }
                }
            }

            // Add new questions (use managed instances for id > 0)
            for (QuizQuestion q : newQuestions) {
                boolean alreadyPresent = quiz.getQuestions().stream()
                        .anyMatch(e -> e.getId() > 0 && q.getId() == e.getId());

                if (!alreadyPresent) {
                    if (q.getId() > 0) {
                        QuizQuestion managed = quizQuestionDao.findById(q.getId()).orElse(q);
                        quiz.addQuestion(managed);
                    } else {
                        quiz.addQuestion(q);
                    }
                }
            }
        }

        Quiz updatedQuiz = quizDao.save(quiz);

        Optional<Quiz> createdQuiz = quizDao.findById(id);
        String creator = createdQuiz.map(Quiz::getCreatedBy).orElse(null);

        return updatedQuiz;
    }

    public void deleteQuiz(int id) {
        quizDao.findById(id).ifPresent(quiz -> quizDao.deleteById(id));
    }

    public Quiz addParticipant(int quizId, String userId) {
        Quiz quiz = getQuizById(quizId);
        if (!quiz.getParticipants().contains(userId)) {
            quiz.getParticipants().add(userId);
            Quiz updatedQuiz = quizDao.save(quiz);
            Optional<Quiz> createdQuiz = quizDao.findById(quizId);
            String creator = createdQuiz.map(Quiz::getCreatedBy).orElse(null);
            emailService.sendParticipantJoinedMailToCreator(updatedQuiz, userId, creator);
            return updatedQuiz;
        }
        return quiz;
    }
}
