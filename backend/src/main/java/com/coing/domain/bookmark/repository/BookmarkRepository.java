package com.coing.domain.bookmark.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.coing.domain.bookmark.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
	boolean existsByUserIdAndMarketCode(UUID userId, String coinCode);

	@Query("SELECT b FROM Bookmark b WHERE b.market.code like CONCAT(:quote, '%')")
	Page<Bookmark> findByUserIdAndQuote(UUID userId, String quote, Pageable pageable);
}