package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPointsDao extends JpaRepository<UserPoints, Integer> {

    Optional<UserPoints> findByClerkId(String clerkId);
}
