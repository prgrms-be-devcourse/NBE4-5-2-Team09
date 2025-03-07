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
    const priceRange = maxPrice - minPrice;

    // 패딩 및 차트 크기 설정
    const padding = { top: 20, right: 60, bottom: 30, left: 100 };
    const chartWidth = rect.width - padding.left - padding.right;
    const chartHeight = rect.height - padding.top - padding.bottom;

    // 스케일링 계산
    const xScale = chartWidth / (candles.length - 1);
    const yScale = chartHeight / priceRange;

    // 가격 축(y축) 그리기
    ctx.beginPath();
    ctx.strokeStyle = "#e5e7eb";
    ctx.lineWidth = 1;

    // 수평 그리드 라인 및 가격 레이블 표시
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

    // 시간 축(x축) 설정
    const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
      month: "numeric",
      day: "numeric",
    });

    // 수직 그리드 라인 및 날짜 레이블 표시
    const numDateLabels = Math.min(candles.length, 10);
    const dateStep = Math.floor(candles.length / numDateLabels);

    for (let i = 0; i < candles.length; i += dateStep) {
      const x = padding.left + i * xScale;
      const date = new Date(candles[i].time);

      ctx.beginPath();
      ctx.strokeStyle = "#e5e7eb";
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, padding.top + chartHeight);
      ctx.stroke();

      // 날짜 레이블
      ctx.fillStyle = "#6b7280";
      ctx.font = "10px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText(
        dateFormatter.format(date),
        x,
        padding.top + chartHeight + 20
      );
    }

    // 캔들 차트 그리기
    candles.forEach((candle, i) => {
      const x = padding.left + i * xScale;
      const open =
        padding.top + chartHeight - (candle.open - minPrice) * yScale;
      const close =
        padding.top + chartHeight - (candle.close - minPrice) * yScale;
      const high =
        padding.top + chartHeight - (candle.high - minPrice) * yScale;
      const low = padding.top + chartHeight - (candle.low - minPrice) * yScale;

      // 심지(고가-저가 선) 그리기
      ctx.beginPath();
      ctx.strokeStyle = candle.open > candle.close ? "#ef4444" : "#10b981";
      ctx.lineWidth = 1;
      ctx.moveTo(x, high);
      ctx.lineTo(x, low);
      ctx.stroke();

      // 몸통(시가-종가 사각형) 그리기
      const candleWidth = xScale * 0.8;
      const bodyHeight = Math.abs(close - open);
      const y = Math.min(open, close);

      ctx.fillStyle = candle.open > candle.close ? "#ef4444" : "#10b981";
      ctx.fillRect(x - candleWidth / 2, y, candleWidth, bodyHeight);
    });
  }, [candles]);

  return (
    <div className="bg-white rounded-lg shadow-sm p-6 h-[500px]">
      {/* 차트 제목 및 기간 선택 버튼 */}
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold">가격 차트</h2>
        <div className="flex space-x-2">
          <button className="px-3 py-1 text-sm bg-blue-50 text-blue-600 rounded-md">
            1일
          </button>
          <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">
            1주
          </button>
          <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">
            1개월
          </button>
          <button className="px-3 py-1 text-sm text-gray-500 hover:bg-gray-100 rounded-md">
            1년
          </button>
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
