package com.web.CertiQuest.dao;

import com.web.CertiQuest.dto.LeaderboardDto;
import com.web.CertiQuest.model.QuizResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardDao extends JpaRepository<QuizResult, Integer> {

    // Global leaderboard: sum of all scores across quizzes
    @Query("""
       SELECT new com.web.CertiQuest.dto.LeaderboardDto(
           q.userId,
           CONCAT(p.firstName, ' ', p.lastName),
           p.photoUrl,
           SUM(q.score),
           COUNT(DISTINCT q.quizId),
           AVG(q.score * 100.0 / q.totalQuestions)
       )
       FROM QuizResult q
       JOIN Profile p ON q.userId = p.clerkId
       GROUP BY q.userId, p.firstName, p.lastName, p.photoUrl
       ORDER BY SUM(q.score) DESC
       """)
    List<LeaderboardDto> findGlobalLeaderboard(Pageable pageable);

    // Per-quiz leaderboard
    @Query("""
       SELECT new com.web.CertiQuest.dto.LeaderboardDto(
           q.userId,
           CONCAT(p.firstName, ' ', p.lastName),
           p.photoUrl,
           CAST(q.score AS long),
           1L,
           (q.score * 100.0 / q.totalQuestions)
       )
       FROM QuizResult q
       JOIN Profile p ON q.userId = p.clerkId
       WHERE q.quizId = :quizId
       ORDER BY q.score DESC
       """)
    List<LeaderboardDto> findQuizLeaderboard(@Param("quizId") int quizId, Pageable pageable);
}
