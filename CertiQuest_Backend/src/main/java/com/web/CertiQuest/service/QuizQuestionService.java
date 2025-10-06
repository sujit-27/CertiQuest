package com.web.CertiQuest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.CertiQuest.dao.QuizQuestionDao;
import com.web.CertiQuest.model.QuizQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
public class QuizQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuizQuestionService.class);

    @Autowired
    private QuizQuestionDao quizQuestionDao;

    @Value("${cohere.api.key}")
    private String cohereApiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public QuizQuestionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ---------------- Prompt builder ----------------
    private String buildPrompt(String category, String difficulty, int noOfQuestions) {
        return String.format("""
                Generate %d multiple-choice quiz questions in the category '%s' with difficulty '%s'.
                Each question must have 4 unique options.
                Format the response strictly as a valid JSON array like this:
                [
                  {
                    "question": "Example question?",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "A"
                  }
                ]
                Do not include explanations or any text outside the JSON array.
                """, noOfQuestions, category, difficulty);
    }

    // ---------------- Call Cohere API ----------------
    private String callCohereAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cohereApiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "command-nightly",
                    "message", prompt,
                    "max_tokens", 3000,  // increased to prevent truncation
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(
                    "https://api.cohere.ai/v1/chat",
                    entity,
                    Map.class
            );

            if (response != null && response.get("text") != null) {
                String rawText = response.get("text").toString().trim();
                String cleaned = cleanJsonResponse(rawText);
                logger.debug("Raw Cohere response: {}", rawText);
                logger.debug("Cleaned JSON response: {}", cleaned);
                return cleaned;
            }

            throw new RuntimeException("No text in Cohere API response: " + response);

        } catch (Exception e) {
            logger.error("Error calling Cohere API: {}", e.getMessage());
            throw new RuntimeException("Error calling Cohere API", e);
        }
    }

    // ---------------- Clean response ----------------
    private String cleanJsonResponse(String text) {
        if (text == null) return null;
        text = text.trim();

        // Remove backticks if Cohere wraps JSON
        if (text.startsWith("```json")) text = text.substring(7).trim();
        else if (text.startsWith("```")) text = text.substring(3).trim();
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3).trim();

        // Remove quotes and escape characters if response is returned as string
        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "")
                    .replace("\\r", "");
        }

        return text;
    }

    // ---------------- Generate questions ----------------
    public List<QuizQuestion> generateQuestions(String category, String difficulty, int noOfQuestions) {
        String prompt = buildPrompt(category, difficulty, noOfQuestions);
        String response = callCohereAPI(prompt);
        return parseQuestions(response, category, difficulty);  // ❌ Do NOT save here
    }

    // ---------------- Get or create quiz ----------------
    public List<QuizQuestion> getOrCreateQuiz(String category, String difficulty, int noOfQuestions) {
        List<QuizQuestion> existing = quizQuestionDao.findByCategoryAndDifficultyLevel(category, difficulty);

        if (!existing.isEmpty() && existing.size() >= noOfQuestions) {
            return existing.subList(0, noOfQuestions);
        }

        String prompt = buildPrompt(category, difficulty, noOfQuestions);
        String response = callCohereAPI(prompt);
        List<QuizQuestion> generated = parseQuestions(response, category, difficulty);
        // Do NOT save here, QuizService will save after setting quizId
        return generated;
    }

    private List<QuizQuestion> parseQuestions(String json, String category, String difficulty) {
        try {
            List<QuizQuestion> questions = objectMapper.readValue(json, new TypeReference<List<QuizQuestion>>() {});
            for (QuizQuestion q : questions) {
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);
            }
            return questions;
        } catch (Exception e) {
            logger.error("Error parsing Cohere JSON response: {}", e.getMessage());
            throw new RuntimeException("Error parsing Cohere response: " + json, e);
        }
    }


    // ---------------- Extract questions from text ----------------
    public List<QuizQuestion> extractQuestionsFromText(String text, String category, String difficulty) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot extract questions: text is empty.");
        }

        String prompt = String.format("""
                Extract quiz questions from the following text in JSON format.
                Include question, 4 options, and correctAnswer.
                Respond strictly as a JSON array:
                Text:
                %s
                """, text);

        String response = callCohereAPI(prompt);
        List<QuizQuestion> questions = parseQuestions(
                response,
                category != null ? category : "General",
                difficulty != null ? difficulty : "Medium"
        );

        if (questions.isEmpty()) {
            throw new RuntimeException("Cohere returned no questions from the text.");
        }

        return questions; // ❌ Do NOT save here, QuizService will handle saving after setting quizId
    }
}
