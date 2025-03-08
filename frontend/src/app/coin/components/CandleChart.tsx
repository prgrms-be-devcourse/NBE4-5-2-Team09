"use client";

import { useEffect, useRef } from "react";
import type { CandleItem } from "@/app/types";

interface CandleChartProps {
  candles: CandleItem[];
}

export default function CandleChart({ candles }: CandleChartProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!canvasRef.current || candles.length === 0) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // 캔버스 크기 설정
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    // 캔버스 초기화
    ctx.clearRect(0, 0, rect.width, rect.height);

    // 최소, 최대 가격 찾기
    const prices = candles.flatMap((candle) => [candle.high, candle.low]);
    const minPrice = Math.min(...prices);
    const maxPrice = Math.max(...prices);
    let priceRange = maxPrice - minPrice;
    // 만약 priceRange가 0이면 기본값을 사용하여 나눗셈 오류를 방지합니다.
    if (priceRange === 0) {
      priceRange = 1;
    }

    // 패딩 및 차트 크기 설정
    const padding = { top: 20, right: 60, bottom: 30, left: 100 };
    const chartWidth = rect.width - padding.left - padding.right;
    const chartHeight = rect.height - padding.top - padding.bottom;

    // y축 스케일 계산
    const yScale = chartHeight / priceRange;

    // 고정 캔들 너비와 간격 설정 (픽셀 단위)
    const candleWidth = 10;
    const candleSpacing = 2;

    // 전체 캔들 영역 너비 계산 및 중앙 배치
    const totalCandlesWidth = candles.length * (candleWidth + candleSpacing) - candleSpacing;
    let offsetX = padding.left;
    if (totalCandlesWidth < chartWidth) {
      offsetX += (chartWidth - totalCandlesWidth) / 2;
    }

    // 가격 축(y축) 그리기
    ctx.beginPath();
    ctx.strokeStyle = "#e5e7eb";
    ctx.lineWidth = 1;
    const numGridLines = 5;
    for (let i = 0; i <= numGridLines; i++) {
      const y = padding.top + chartHeight - (i / numGridLines) * chartHeight;
      const price = minPrice + (i / numGridLines) * priceRange;
      ctx.moveTo(padding.left, y);
      ctx.lineTo(padding.left + chartWidth, y);
      // 가격 레이블
      ctx.fillStyle = "#6b7280";
      ctx.font = "10px sans-serif";
      ctx.textAlign = "right";
      ctx.fillText(
          price.toLocaleString(undefined, {
            minimumFractionDigits: 3,
            maximumFractionDigits: 3,
          }),
          padding.left - 10,
          y + 4
      );
    }
    ctx.stroke();

    // x축(시간 축) 그리기: 날짜 레이블을 일정 간격으로 표시
    const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
      month: "numeric",
      day: "numeric",
    });
    const numDateLabels = Math.min(candles.length, 10);
    const step = Math.floor(candles.length / numDateLabels);
    for (let i = 0; i < candles.length; i += step) {
      // 각 캔들의 중앙을 기준으로 x 좌표 계산
      const x = offsetX + i * (candleWidth + candleSpacing) + candleWidth / 2;
      const date = new Date(candles[i].time);
      ctx.beginPath();
      ctx.strokeStyle = "#e5e7eb";
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, padding.top + chartHeight);
      ctx.stroke();

      ctx.fillStyle = "#6b7280";
      ctx.font = "10px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText(dateFormatter.format(date), x, padding.top + chartHeight + 20);
    }

    // 캔들 차트 그리기
    candles.forEach((candle, i) => {
      // x 좌표: 중앙을 기준으로 계산
      const x = offsetX + i * (candleWidth + candleSpacing) + candleWidth / 2;
      const openY = padding.top + chartHeight - (candle.open - minPrice) * yScale;
      const closeY = padding.top + chartHeight - (candle.close - minPrice) * yScale;
      const highY = padding.top + chartHeight - (candle.high - minPrice) * yScale;
      const lowY = padding.top + chartHeight - (candle.low - minPrice) * yScale;

      // 심지 그리기 (고가-저가 선)
      ctx.beginPath();
      ctx.strokeStyle = candle.open > candle.close ? "#ef4444" : "#10b981";
      ctx.lineWidth = 1;
      ctx.moveTo(x, highY);
      ctx.lineTo(x, lowY);
      ctx.stroke();

      // 몸통 그리기 (시가-종가 사각형)
      const bodyTop = Math.min(openY, closeY);
      const bodyHeight = Math.abs(closeY - openY);
      ctx.fillStyle = candle.open > candle.close ? "#ef4444" : "#10b981";
      ctx.fillRect(x - candleWidth / 2, bodyTop, candleWidth, bodyHeight);
    });
  }, [candles]);

  return (
      <div className="bg-white rounded-lg shadow-sm p-6 h-[500px]">
        {/* 차트 제목 및 기간 선택 버튼 */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold">가격 차트</h2>
          <div className="flex space-x-2">
            <button className="px-3 py-1 text-sm bg-blue-50 text-blue-600 rounded-md">1일</button>
            <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">1주</button>
            <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">1개월</button>
            <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">1년</button>
          </div>
        </div>
        {/* 캔들 차트 캔버스 */}
        <div className="h-[calc(100%-2rem)]">
          <canvas
              ref={canvasRef}
              className="w-full h-full"
              style={{ width: "100%", height: "100%" }}
          />
        </div>
      </div>
  );
}
