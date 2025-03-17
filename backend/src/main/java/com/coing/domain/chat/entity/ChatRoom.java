package com.coing.domain.chat.entity;

import java.time.LocalDateTime;

import com.coing.domain.coin.market.entity.Market;
import com.coing.util.BaseEntity;

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
import lombok.Setter;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 해당 채팅방이 연결된 마켓 (예: "KRW-BTC")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "market_id", nullable = false)
	private Market market;

	// 채팅방 이름 (마켓의 한글 이름 등으로 지정 가능)
	@Column(nullable = false)
	private String name;

	// 채팅방 생성 시간
	private LocalDateTime createdAt;

	// 채팅방을 생성할 때 생성 시간을 자동으로 설정하는 로직 추가 가능
}
