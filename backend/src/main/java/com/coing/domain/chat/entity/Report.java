package com.coing.domain.chat;

import java.time.LocalDateTime;

import com.coing.util.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 신고 대상 채팅 메시지의 ID
	private Long messageId;

	// 신고한 사용자(예: 이메일이나 사용자 ID)
	private String reporter;

	// 신고 사유
	private String reason;

	// 신고 시각
	private LocalDateTime reportTime;
}
