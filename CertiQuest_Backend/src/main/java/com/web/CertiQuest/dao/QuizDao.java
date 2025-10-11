package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizDao extends JpaRepository<Quiz, Integer> {

    List<Quiz> findByCreatedBy(String clerkId);
}
