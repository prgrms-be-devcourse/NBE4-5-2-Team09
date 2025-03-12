package com.coing.domain.coin.market.entity;

import com.coing.util.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Market extends BaseEntity {

	@Id
	@Column(name = "market_id")
	private String code;

	@Column(name = "korean_name", nullable = false)
	private String koreanName;

	@Column(name = "english_name", nullable = false)
	private String englishName;

}
