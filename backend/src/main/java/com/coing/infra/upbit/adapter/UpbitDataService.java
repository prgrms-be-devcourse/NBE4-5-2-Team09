package com.coing.infra.upbit.adapter;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.entity.Orderbook;
import com.coing.infra.upbit.dto.OrderbookDto;

/**
 *  Upbit WebSocket 수신 데이터를 처리하고 관리하는 서비스 계층
 *  데이터를 가공 및 캐싱하여 데이터베이스에 저장하기 위한 비즈니스 로직 담당
 *
 *  TODO: DB 확정 시 Repository 대체
 */
@Service
public class UpbitDataService {

    private static final Logger logger = LoggerFactory.getLogger(UpbitDataService.class);

    private final AtomicReference<Orderbook> lastOrderbook = new AtomicReference<>();

    public void processOrderbookData(OrderbookDto orderbookDto) {
        Orderbook orderbook = orderbookDto.toEntity();
        lastOrderbook.set(orderbook);
    }

    public Orderbook getLastOrderbook() {
        return lastOrderbook.get();
    }
}
