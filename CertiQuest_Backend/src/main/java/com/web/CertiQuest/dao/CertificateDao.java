package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateDao extends JpaRepository<Certificate, Long> {

    // Fetch certificates by user
    List<Certificate> findByUserId(String userId);

    // Optional: fetch certificate for a specific quiz
    Certificate findByUserIdAndQuizId(String userId, int quizId);

    List<Certificate> findAllByUserId(String userId);

    List<Certificate> findALlByUserId(java.lang.String userId);
}

