package com.coing.domain.coin.market.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.coin.market.entity.Market;

@Repository
public interface MarketRepository extends JpaRepository<Market, String> {
	Page<Market> findByCodeStartingWith(String prefix, Pageable pageable);
}
