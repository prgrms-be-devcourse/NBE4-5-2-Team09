package com.coing.domain.bookmark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coing.domain.bookmark.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
	List<Bookmark> findByUserId(Long userId);

	boolean existsByUserIdAndCoinCode(Long userId, String coinCode);
}