package com.coing.domain.bookmark.entity;

import java.time.LocalDateTime;

import com.coing.domain.coin.market.entity.Market;
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

	// 북마크를 등록한 회원
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 북마크 대상 마켓 (코인 ID와 동일한 역할을 함; 예: "KRW-BTC")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coin_id", nullable = false)
	private Market market;

	@Column(nullable = false, name = "create_at")
	private LocalDateTime createAt;

	@Column(name = "update_at")
	private LocalDateTime updateAt;

	// 마켓 정보를 업데이트할 때 사용하는 메서드
	public void updateMarket(Market newMarket) {
		this.market = newMarket;
		this.updateAt = LocalDateTime.now();
	}
}
