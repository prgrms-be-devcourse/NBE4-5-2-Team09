package com.coing.domain.bookmark.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coing.domain.bookmark.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
	List<Bookmark> findByUserId(UUID userId);

	boolean existsByUserIdAndMarketCode(UUID userId, String coinCode);
}