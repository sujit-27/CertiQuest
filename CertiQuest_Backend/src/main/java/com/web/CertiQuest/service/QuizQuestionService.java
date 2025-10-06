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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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
                    "max_tokens", 3000,
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

        if (text.startsWith("```json")) text = text.substring(7).trim();
        else if (text.startsWith("```")) text = text.substring(3).trim();
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3).trim();

        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "")
                    .replace("\\r", "");
        }

        return text;
    }

    // ---------------- Generate questions ----------------
    public List<QuizQuestion> generateQuestions(String category, String difficulty, int count) {
        List<QuizQuestion> generated = new ArrayList<>();

        try {
            // Generate prompt
            String prompt = buildPrompt(category, difficulty, count);
            String jsonResponse = callCohereAPI(prompt);

            // Parse JSON into QuizQuestion objects
            List<QuizQuestion> aiQuestions = parseQuestions(jsonResponse, category, difficulty);
            generated.addAll(aiQuestions);

        } catch (Exception e) {
            logger.error("AI generation failed: {}, using fallback.", e.getMessage());

            // Fallback: generate dummy questions
            for (int i = 1; i <= count; i++) {
                QuizQuestion q = new QuizQuestion();
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);
                q.setQuestion("Sample question " + i + " for " + category + " (" + difficulty + ")");
                q.setOptions(List.of("Option A", "Option B", "Option C", "Option D"));
                q.setCorrectAnswer("Option A");
                generated.add(q);
            }
        }

        return generated;
    }

    // ---------------- Get or create quiz ----------------
    @Transactional(readOnly = true)
    public List<QuizQuestion> getOrCreateQuiz(String category, String difficulty, int noOfQuestions) {
        List<QuizQuestion> existing = quizQuestionDao.findByCategoryAndDifficultyLevel(category, difficulty);

        if (existing.size() >= noOfQuestions) {
            Collections.shuffle(existing);
            return existing.stream()
                    .limit(noOfQuestions)
                    .collect(Collectors.toList());
        }

        int needed = noOfQuestions - existing.size();
        List<QuizQuestion> generated = generateQuestions(category, difficulty, needed);

        List<QuizQuestion> result = new ArrayList<>(existing);
        result.addAll(generated);
        return result;
    }

    // ---------------- Parse questions ----------------
    private List<QuizQuestion> parseQuestions(String json, String category, String difficulty) {
        try {
            List<Map<String, Object>> questionMaps = objectMapper.readValue(json, new TypeReference<>() {});
            List<QuizQuestion> questions = new ArrayList<>();

            for (Map<String, Object> map : questionMaps) {
                QuizQuestion q = new QuizQuestion();
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);
                q.setQuestion((String) map.get("question"));

                // Handle options (ensure List<String>)
                Object opts = map.get("options");
                if (opts instanceof List<?>) {
                    q.setOptions(((List<?>) opts).stream()
                            .map(Object::toString)
                            .collect(Collectors.toList()));
                } else {
                    q.setOptions(List.of("Option A", "Option B", "Option C", "Option D"));
                }

                q.setCorrectAnswer((String) map.getOrDefault("correctAnswer", "Option A"));
                questions.add(q);
            }

            return questions;

        } catch (Exception e) {
            logger.error("Error parsing Cohere JSON response: {}", e.getMessage());
            throw new RuntimeException("Error parsing Cohere response: " + json, e);
        }
    }

    // ---------------- Extract questions from text ----------------
    public List<QuizQuestion> extractQuestionsFromText(String pdfText, String category, String difficulty) {
        List<QuizQuestion> extracted = new ArrayList<>();
        // Split by pages using form-feed or multiple newlines (covers most PDF text extractors)
        String[] pages = pdfText.split("\\f|\\r?\\n\\s*\\r?\\n+");

        for (String page : pages) {
            // Split questions per page using patterns like "1." or "Question 1"
            String[] parts = page.split("(?i)(?:^|\\s)question\\s*\\d+[:.-]|(?:^|\\s)\\d+\\.\\s+");
            for (String part : parts) {
                if (part.trim().isEmpty()) continue;

                QuizQuestion q = new QuizQuestion();
                q.setCategory(category);
                q.setDifficultyLevel(difficulty);

                // Try to separate question and options
                String[] lines = part.trim().split("\\r?\\n");
                // Find the first non-option, non-answer line as the question
                String questionText = Arrays.stream(lines)
                        .filter(l -> !l.trim().isEmpty() && !l.matches("^[A-Da-d][\\).].*") && !l.toLowerCase().contains("answer:"))
                        .findFirst()
                        .orElse("Sample question");
                q.setQuestion(questionText.trim());

                // Extract options: lines starting with A/B/C/D or similar
                List<String> opts = Arrays.stream(lines)
                        .filter(l -> l.matches("^[A-Da-d1-4][\\).>].*"))  // accept options starting with A-D, a-d, 1-4 followed by ), ., or >
                        .map(l -> l.replaceFirst("^[A-Da-d1-4][\\).>]+\\s*", ""))  // remove prefix + whitespace
                        .collect(Collectors.toList());
                // Ensure 4 options
                if (opts.size() < 4) {
                    opts = List.of("Option A", "Option B", "Option C", "Option D");
                }
                q.setOptions(opts);

                // Extract correct answer
                List<String> finalOpts = opts;
                String correct = Arrays.stream(lines)
                        .filter(l -> l.toLowerCase().contains("answer:"))
                        .findFirst()
                        .map(l -> {
                            String s = l.substring(l.toLowerCase().indexOf("answer:") + 7).trim();
                            // Remove leading label such as 'b)', 'a.', '1.' if present
                            s = s.replaceFirst("^[\\dA-Da-d][\\).>\\]]?\\s*", "");
                            // Try to match single letter answer to option text
                            if (s.matches("^[A-Da-d]$")) {
                                int idx = "ABCD".indexOf(s.toUpperCase());
                                return idx >= 0 && idx < finalOpts.size() ? finalOpts.get(idx) : finalOpts.get(0);
                            } else {
                                return s;
                            }
                        })
                        .orElse(opts.get(0));
                q.setCorrectAnswer(correct);


                extracted.add(q);
            }
        }

        return extracted;
    }

}
