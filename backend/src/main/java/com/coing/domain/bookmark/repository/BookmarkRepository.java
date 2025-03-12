package com.coing.domain.bookmark.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coing.domain.bookmark.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
	boolean existsByUserIdAndMarketCode(UUID userId, String coinCode);

	@Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.market.code like CONCAT(:quote, '%')")
	List<Bookmark> findByUserIdAndQuote(@Param("userId") UUID userId, @Param("quote") String quote);

	Bookmark findByMarketCode(String code);
}
