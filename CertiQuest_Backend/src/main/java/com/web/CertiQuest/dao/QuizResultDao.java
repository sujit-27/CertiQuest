package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultDao extends JpaRepository<QuizResult, Integer> {

    List<QuizResult> findByUserId(String userId);
    List<QuizResult> findByQuizId(int quizId);
}
