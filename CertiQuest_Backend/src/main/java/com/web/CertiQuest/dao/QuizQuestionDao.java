package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionDao extends JpaRepository<QuizQuestion,Integer> {

    List<QuizQuestion> findByCategoryAndDifficultyLevel(String category, String difficultyLevel);
}
