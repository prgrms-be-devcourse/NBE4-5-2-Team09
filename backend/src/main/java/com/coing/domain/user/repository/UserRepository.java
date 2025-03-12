package com.coing.domain.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coing.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	public Optional<User> findByEmail(String email);

	@Modifying
	@Query("DELETE FROM User u WHERE u.verified = false AND u.createdAt < :threshold")
	int deleteUnverifiedUsers(@Param("threshold") LocalDateTime threshold);
}
