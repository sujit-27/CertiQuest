package com.web.CertiQuest.dao;

import com.web.CertiQuest.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileDao extends JpaRepository<Profile, Integer> {

    Optional<Profile> findByEmail(String email);

    Profile findByClerkId(String clerkId);

    Boolean existsByClerkId(String clerkId);

}
