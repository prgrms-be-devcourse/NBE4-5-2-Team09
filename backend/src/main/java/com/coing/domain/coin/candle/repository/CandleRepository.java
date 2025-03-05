package com.coing.domain.coin.candle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.coin.candle.entity.Candle;

@Repository
public interface CandleRepository extends JpaRepository<Candle, Long> {
	// 추가적인 쿼리 메서드가 필요하다면 여기에 정의합니다.
}