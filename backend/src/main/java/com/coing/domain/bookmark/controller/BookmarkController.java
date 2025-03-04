package com.coing.domain.bookmark.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.controller.dto.BookmarkUpdateRequest;
import com.coing.domain.bookmark.service.BookmarkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

	private final BookmarkService bookmarkService;

	// 북마크 등록
	@PostMapping("/create")
	public ResponseEntity<BookmarkResponse> addBookmark(@RequestBody @Validated BookmarkRequest request) {
		BookmarkResponse response = bookmarkService.addBookmark(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 회원별 북마크 조회
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookmarkResponse>> getBookmarksByUser(@PathVariable Long userId) {
		List<BookmarkResponse> responses = bookmarkService.getBookmarksByUser(userId);
		return ResponseEntity.ok(responses);
	}

	// 북마크 수정
	@PutMapping
	public ResponseEntity<BookmarkResponse> updateBookmark(@RequestBody @Validated BookmarkUpdateRequest request) {
		BookmarkResponse response = bookmarkService.updateBookmark(request);
		return ResponseEntity.ok(response);
	}

	// 북마크 삭제
	@DeleteMapping("/{bookmarkId}")
	public ResponseEntity<Void> deleteBookmark(@PathVariable Long bookmarkId) {
		bookmarkService.deleteBookmark(bookmarkId);
		return ResponseEntity.noContent().build();
	}
}
