"use client";

import React, { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import CandleChart from "@/app/coin/components/CandleChart";
import type { CandleItem, CandleChartDto } from "@/app/types";

interface CandleChartContainerProps {
    market: string;
    interval?: string; // 기본값은 "day"
}

export default function CandleChartContainer({
                                                 market,
                                                 interval = "day",
                                             }: CandleChartContainerProps) {
    const [candles, setCandles] = useState<CandleItem[]>([]);

    useEffect(() => {
        const client = new Client({
            brokerURL: "ws://localhost:8080/websocket", // 백엔드 웹소켓 엔드포인트
            reconnectDelay: 5000,
            debug: (str) => console.log(str),
        });

        client.onConnect = (frame) => {
            console.log("STOMP 연결 성공:", frame);
            // 백엔드와 토픽 규칙이 일치하도록 market은 대문자, interval은 소문자로 사용
            const topic = `/sub/coin/candles/${market.toUpperCase()}/${interval.toLowerCase()}`;
            console.log("구독할 토픽:", topic);
            const subscription = client.subscribe(topic, (message) => {
                try {
                    const dto: CandleChartDto = JSON.parse(message.body);
                    // 변환: 백엔드 DTO → CandleItem
                    const newCandle: CandleItem = {
                        time: new Date(dto.candleDateTime).getTime(),
                        open: dto.openingPrice,
                        high: dto.highPrice,
                        low: dto.lowPrice,
                        close: dto.closingPrice,
                        volume: dto.volume,
                    };
                    setCandles((prev) => {
                        const updated = [...prev, newCandle];
                        return updated.length > 50 ? updated.slice(updated.length - 50) : updated;
                    });
                } catch (error) {
                    console.error("캔들 데이터 파싱 오류:", error);
                }
            });
            return () => subscription.unsubscribe();
        };

        client.onStompError = (frame) => {
            console.error("STOMP 에러:", frame);
        };

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [market, interval]);

    return (
        <div>
            <h2 className="text-2xl font-bold mb-4">실시간 캔들 차트</h2>
            <CandleChart candles={candles} />
        </div>
    );
}
