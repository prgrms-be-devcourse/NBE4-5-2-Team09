package com.coing.domain.bookmark.entity;

import java.time.LocalDateTime;

import com.coing.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookmark")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Bookmark {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bookmark_id")
	private Long id;

	// 북마크를 등록한 회원의 식별자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 북마크 대상 코인 코드 (예: "KRW-BTC")
	@Column(nullable = false, name = "coin_id")
	private String coinCode;

	@Column(nullable = false, name = "create_at")
	private LocalDateTime createAt;

	@Column(name = "update_at")
	private LocalDateTime updateAt;

	public void updateCoinCode(String newCoinCode) {
		this.coinCode = newCoinCode;
		this.updateAt = LocalDateTime.now();
	}
}