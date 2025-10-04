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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizQuestionService {

    @Autowired
    private QuizQuestionDao quizQuestionDao;

    @Value("${cohere.api.key}")
    private String cohereApiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public QuizQuestionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private String buildPrompt(String category, String difficulty, int noOfQuestions) {
        return String.format("""
                Generate %d multiple-choice quiz questions in the category '%s' with difficulty '%s'.
                Each question must have 4 unique options.
                Format the response strictly as a valid JSON array like:
                [
                  {
                    "question": "Which planet is known as the Red Planet?",
                    "options": ["Earth", "Mars", "Jupiter", "Saturn"],
                    "correctAnswer": "Mars"
                  }
                ]
                Do not include any text outside the JSON array. Do not include explanations.
                """, noOfQuestions, category, difficulty);
    }

    private String callCohereAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + cohereApiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "command-nightly",
                    "message", prompt,
                    "max_tokens", 1500,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(
                    "https://api.cohere.ai/v1/chat",
                    entity,
                    Map.class
            );

            if (response != null && response.get("text") != null) {
                String text = response.get("text").toString().trim();
                return cleanJsonResponse(text);
            }
            throw new RuntimeException("No text in Cohere API response: " + response);
        } catch (Exception e) {
            throw new RuntimeException("Error calling Cohere API: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.startsWith("```json")) text = text.substring(7).trim();
        else if (text.startsWith("```")) text = text.substring(3).trim();
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3).trim();
        return text;
    }

    public List<QuizQuestion> generateQuestions(String category, String difficulty, int noOfQuestions) {
        String prompt = buildPrompt(category, difficulty, noOfQuestions);
        String response = callCohereAPI(prompt);

        try {
            List<QuizQuestion> questions = objectMapper.readValue(response, new TypeReference<>() {
            });
            for (QuizQuestion q : questions) {
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);
            }
            quizQuestionDao.saveAll(questions);
            return questions;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Cohere response: " + response, e);
        }
    }

    public List<QuizQuestion> getOrCreateQuiz(String category, String difficulty, int noOfQuestions) {
        List<QuizQuestion> existing = quizQuestionDao.findByCategoryAndDifficultyLevel(category, difficulty);
        if (!existing.isEmpty() && existing.size() >= noOfQuestions)
            return existing.subList(0, noOfQuestions);

        String prompt = buildPrompt(category, difficulty, noOfQuestions);
        String response = callCohereAPI(prompt);

        try {
            List<QuizQuestion> newQuestions = objectMapper.readValue(response, new TypeReference<>() {});
            for (QuizQuestion q : newQuestions) {
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);
            }
            // Do NOT save yet; QuizService will assign quiz and save
            return newQuestions;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Cohere response: " + response, e);
        }
    }

    public List<QuizQuestion> extractQuestionsFromText(String text, String category, String difficulty) {
        if (text == null || text.trim().isEmpty())
            throw new IllegalArgumentException("Cannot extract questions: PDF contains no text.");

        String prompt = String.format("""
            Extract quiz questions from the following text in JSON format.
            Include question, 4 options, and correctAnswer.
            Respond strictly as a JSON array like:
            [
              {
                "question": "Example question?",
                "options": ["A", "B", "C", "D"],
                "correctAnswer": "B"
              }
            ]
            Text:
            %s
            """, text);

        String response = callCohereAPI(prompt);

        try {
            List<QuizQuestion> questions = objectMapper.readValue(response, new TypeReference<>() {});
            if (questions == null || questions.isEmpty())
                throw new RuntimeException("Cohere returned no questions from PDF text.");

            for (QuizQuestion q : questions) {
                q.setCategory(category != null ? category : "General");
                q.setDifficultyLevel(difficulty != null ? difficulty : "Medium");
            }
            return questions;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Cohere response: " + e.getMessage(), e);
        }
    }

    public void saveAllQuestions(List<QuizQuestion> questions) {
        if (questions != null && !questions.isEmpty())
            quizQuestionDao.saveAll(questions);
    }

    public Optional<QuizQuestion> findById(int id) {
        return quizQuestionDao.findById(id);
    }
}
