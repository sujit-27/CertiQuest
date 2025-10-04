package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.QuizDao;
import com.web.CertiQuest.dao.QuizResultDao;
import com.web.CertiQuest.dao.UserPointsDao;
import com.web.CertiQuest.dto.QuizSubmissionDto;
import com.web.CertiQuest.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QuizService {

    @Autowired
    private QuizDao quizDao;
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
    KafkaTemplate<String, QuizEvent> kafkaTemplate;

    private static final String TOPIC = "quiz-admin";

    public Quiz createQuiz(String title, String category, String difficulty, int noOfQuestions, String createdBy) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to create quiz"));

        if (noOfQuestions <= 0)
            throw new IllegalArgumentException("Number of questions must be greater than zero.");

        Profile creator = profileService.getCurrentProfile();
        validateAdminPlanForQuizCreation(creator, difficulty, noOfQuestions);

        // Get questions from service but do not save yet
        List<QuizQuestion> questions = quizQuestionService.getOrCreateQuiz(category, difficulty, noOfQuestions);

        // Create quiz entity
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setDifficulty(difficulty);
        quiz.setNoOfQuestions(noOfQuestions);
        quiz.setCreatedAt(Instant.now());
        quiz.setCreatedBy(createdBy);
        quiz.setExpiryDate(LocalDate.now().plusDays(7));

        Quiz savedQuiz = quizDao.save(quiz);

        // Assign quiz reference to each question and save
        for (QuizQuestion q : questions) {
            q.setQuiz(savedQuiz);
        }
        quizQuestionService.saveAllQuestions(questions);
        savedQuiz.setQuestions(questions);

        profileService.incrementQuizCreatedCount(creator);

        kafkaTemplate.send(TOPIC, new QuizEvent(
                "CREATED",
                savedQuiz.getId(),
                savedQuiz.getTitle(),
                savedQuiz.getDifficulty(),
                savedQuiz.getQuestions().size()
        ));

        return savedQuiz;
    }

    public Quiz createQuizFromPdf(MultipartFile pdfFile, String title, String category, String difficulty, String createdBy) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to create quiz"));

        if (pdfFile == null || pdfFile.isEmpty())
            throw new IllegalArgumentException("PDF file is required.");
        if (!"application/pdf".equalsIgnoreCase(pdfFile.getContentType()))
            throw new IllegalArgumentException("Only PDF files are supported.");

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
                if (pdfText.trim().isEmpty())
                    throw new RuntimeException("Cannot extract questions: PDF contains no text even after OCR.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF file", e);
        }

        List<QuizQuestion> questions = quizQuestionService.extractQuestionsFromText(
                pdfText,
                category != null ? category : "General",
                difficulty != null ? difficulty : "Medium"
        );

        if (questions.isEmpty())
            throw new RuntimeException("No quiz questions could be extracted from PDF.");

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

        Quiz savedQuiz = quizDao.save(quiz);

        for (QuizQuestion q : questions) {
            q.setQuiz(savedQuiz);
        }
        quizQuestionService.saveAllQuestions(questions);
        savedQuiz.setQuestions(questions);

        profileService.incrementQuizCreatedCount(creator);

        kafkaTemplate.send(TOPIC, new QuizEvent(
                "CREATED",
                savedQuiz.getId(),
                savedQuiz.getTitle(),
                savedQuiz.getDifficulty(),
                savedQuiz.getQuestions().size()
        ));

        return savedQuiz;
    }

    private void validateAdminPlanForQuizCreation(Profile user, String difficulty, int noOfQuestions) {
        String userId = user.getClerkId();
        Optional<UserPoints> userPoints = userPointsDao.findByClerkId(userId);
        String plan = userPoints.get().getPlan();

        switch (plan) {
            case "BASIC" -> {
                if (noOfQuestions > 10)
                    throw new RuntimeException("Free plan users can only create quizzes with up to 10 questions.");
                if ("HARD".equalsIgnoreCase(difficulty))
                    throw new RuntimeException("Free plan users cannot create HARD difficulty quizzes.");
                int createdThisMonth = profileService.getQuizzesCreatedThisMonth(user.getId());
                if (createdThisMonth >= 5)
                    throw new RuntimeException("Free plan users can only create 5 quizzes per month.");
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
        AtomicInteger score = new AtomicInteger();
        for (QuizSubmissionDto.UserAnswer ans : submission.getAnswers()) {
            quizQuestionService.findById(ans.getQuestionId())
                    .ifPresent(question -> {
                        if (question.getCorrectAnswer().equalsIgnoreCase(ans.getSelectedAnswer())) {
                            score.getAndIncrement();
                        }
                    });
        }
        return score.get();
    }

    public QuizResult evaluateAndSaveResult(QuizSubmissionDto submission, String userId) {
        userPointsService.consumePoints(1)
                .orElseThrow(() -> new RuntimeException("Insufficient points to attend quiz"));

        int score = calculateScore(submission);

        QuizResult result = new QuizResult();
        result.setQuizId(submission.getQuizId());
        result.setUserId(userId);
        result.setScore(score);
        result.setTotalQuestions(submission.getAnswers().size());
        quizResultDao.save(result);

        Quiz quiz = getQuizById(submission.getQuizId());
        if (!quiz.getParticipants().contains(userId)) {
            quiz.getParticipants().add(userId);
            quizDao.save(quiz);
        }

        return result;
    }
}
