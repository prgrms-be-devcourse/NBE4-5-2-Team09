package com.coing.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	public Optional<User> findByEmail(String email);
}
