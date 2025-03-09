"use client";

import { useEffect, useRef, useState } from "react";
import type { CandleItem } from "@/app/types";

interface CandleChartProps {
    candles: CandleItem[];
    candleType: "seconds" | "minutes" | "days" | "weeks" | "months" | "years";
    setCandleType: (type: "seconds" | "minutes" | "days" | "weeks" | "months" | "years") => void;
}

export default function CandleChart({ candles, candleType, setCandleType }: CandleChartProps) {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [zoom, setZoom] = useState(1); // 캔들 그리기에만 적용되는 줌
    const [crosshair, setCrosshair] = useState<{ x: number; y: number } | null>(null);

    useEffect(() => {
        if (!canvasRef.current || candles.length === 0) return;

        // 시간 기준 오름차순 정렬
        const sortedCandles = [...candles].sort((a, b) => a.time - b.time);

        const canvas = canvasRef.current;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        // 캔버스 DPI 처리
        const dpr = window.devicePixelRatio || 1;
        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        ctx.scale(dpr, dpr);

        const drawChart = () => {
            ctx.clearRect(0, 0, rect.width, rect.height);

            // 가격 범위 계산
            const prices = sortedCandles.flatMap(c => [c.high, c.low]);
            const minPrice = Math.min(...prices);
            const maxPrice = Math.max(...prices);
            let priceRange = maxPrice - minPrice;
            if (priceRange === 0) priceRange = 1;

            // 차트 영역 설정
            const padding = { top: 20, right: 60, bottom: 30, left: 100 };
            const chartWidth = rect.width - padding.left - padding.right;
            const chartHeight = rect.height - padding.top - padding.bottom;
            const yScale = chartHeight / priceRange;

            // x축: 시간 기반, 최신(마지막) 캔들을 기준으로 계산 (오른쪽 고정)
            const lastTime = sortedCandles[sortedCandles.length - 1].time;
            const firstTime = sortedCandles[0].time;
            const timeRange = lastTime - firstTime;
            const baseXScale = timeRange === 0 ? 1 : chartWidth / timeRange;
            const candleXScale = baseXScale * zoom;

            // grid용 x 좌표: 기본 스케일 사용
            const calcXBase = (time: number) =>
                padding.left + chartWidth - ((lastTime - time) * baseXScale);
            // 캔들용 x 좌표: 줌 적용
            const calcXCandle = (time: number) =>
                padding.left + chartWidth - ((lastTime - time) * candleXScale);

            // ────────────
            // y축 그리드 및 레이블 (grid는 고정)
            ctx.beginPath();
            ctx.strokeStyle = "#e5e7eb";
            ctx.lineWidth = 1;
            const numGridLines = 5;
            for (let i = 0; i <= numGridLines; i++) {
                const y = padding.top + chartHeight - (i / numGridLines) * chartHeight;
                const price = minPrice + (i / numGridLines) * priceRange;
                ctx.moveTo(padding.left, y);
                ctx.lineTo(padding.left + chartWidth, y);
                ctx.fillStyle = "#6b7280";
                ctx.font = "10px sans-serif";
                ctx.textAlign = "right";
                ctx.fillText(
                    price.toLocaleString("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
                    padding.left - 10,
                    y + 4
                );
            }
            ctx.stroke();

            // x축 레이블 (grid용: 기본 스케일)
            const numXLabels = 10;
            let dateFormatter: Intl.DateTimeFormat;
            switch (candleType) {
                case "seconds":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false });
                    break;
                case "minutes":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { hour: "2-digit", minute: "2-digit", hour12: false });
                    break;
                case "days":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { month: "2-digit", day: "2-digit" });
                    break;
                case "weeks":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { month: "2-digit", day: "2-digit" });
                    break;
                case "months":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { year: "numeric", month: "2-digit" });
                    break;
                case "years":
                    dateFormatter = new Intl.DateTimeFormat("ko-KR", { year: "numeric" });
                    break;
                default:
                    dateFormatter = new Intl.DateTimeFormat("ko-KR");
            }
            for (let i = 0; i <= numXLabels; i++) {
                const labelTime = lastTime - (timeRange * i) / numXLabels;
                const x = calcXBase(labelTime);
                ctx.beginPath();
                ctx.strokeStyle = "#e5e7eb";
                ctx.moveTo(x, padding.top);
                ctx.lineTo(x, padding.top + chartHeight);
                ctx.stroke();
                ctx.fillStyle = "#6b7280";
                ctx.font = "10px sans-serif";
                ctx.textAlign = "center";
                const kstTime = new Date(labelTime + 9 * 3600 * 1000);
                ctx.fillText(dateFormatter.format(kstTime), x, padding.top + chartHeight + 20);
            }
            // ────────────

            // 클리핑: 캔들 그리기 영역만 확대/축소 적용
            ctx.save();
            ctx.beginPath();
            ctx.rect(padding.left, padding.top, chartWidth, chartHeight);
            ctx.clip();

            // 캔들 그리기 (줌 적용)
            const candleWidth = 10; // 고정 캔들 너비
            sortedCandles.forEach(candle => {
                const x = calcXCandle(candle.time);
                const openY = padding.top + chartHeight - (candle.open - minPrice) * yScale;
                const closeY = padding.top + chartHeight - (candle.close - minPrice) * yScale;
                const highY = padding.top + chartHeight - (candle.high - minPrice) * yScale;
                const lowY = padding.top + chartHeight - (candle.low - minPrice) * yScale;

                const isUp = candle.close > candle.open;
                const color = isUp ? "red" : "blue";

                ctx.beginPath();
                ctx.strokeStyle = color;
                ctx.lineWidth = 1;
                ctx.moveTo(x, highY);
                ctx.lineTo(x, lowY);
                ctx.stroke();

                const bodyTop = Math.min(openY, closeY);
                const bodyHeight = Math.max(Math.abs(closeY - openY), 1);
                ctx.fillStyle = color;
                ctx.fillRect(x - candleWidth / 2, bodyTop, candleWidth, bodyHeight);
            });
            ctx.restore();

            // Crosshair 그리기 (마우스가 캔버스 위에 있을 때)
            if (crosshair) {
                ctx.beginPath();
                ctx.strokeStyle = "gray";
                ctx.lineWidth = 1;
                ctx.moveTo(crosshair.x, padding.top);
                ctx.lineTo(crosshair.x, padding.top + chartHeight);
                ctx.moveTo(padding.left, crosshair.y);
                ctx.lineTo(padding.left + chartWidth, crosshair.y);
                ctx.stroke();

                const timeAtCross = lastTime - ((padding.left + chartWidth - crosshair.x) / candleXScale);
                const kstAtCross = new Date(timeAtCross + 9 * 3600 * 1000);
                const priceAtCross = maxPrice - ((crosshair.y - padding.top) / yScale);
                ctx.fillStyle = "black";
                ctx.font = "12px sans-serif";
                ctx.textAlign = "left";
                ctx.fillText(`Time: ${kstAtCross.toLocaleTimeString("ko-KR", { hour12: false })}`, crosshair.x + 5, crosshair.y - 20);
                ctx.fillText(`Price: ${priceAtCross.toLocaleString("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, crosshair.x + 5, crosshair.y - 5);
            }
        };

        drawChart();

        const handleWheel = (e: WheelEvent) => {
            e.preventDefault();
            setZoom(prev => {
                const newZoom = e.deltaY < 0 ? prev * 1.1 : prev / 1.1;
                return Math.min(Math.max(newZoom, 0.5), 5);
            });
        };

        const handleMouseMove = (e: MouseEvent) => {
            const rect = canvas.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            setCrosshair({ x, y });
            drawChart();
        };

        const handleMouseLeave = (e: MouseEvent) => {
            setCrosshair(null);
            drawChart();
        };

        canvas.addEventListener("wheel", handleWheel, { passive: false });
        canvas.addEventListener("mousemove", handleMouseMove);
        canvas.addEventListener("mouseleave", handleMouseLeave);

        return () => {
            canvas.removeEventListener("wheel", handleWheel);
            canvas.removeEventListener("mousemove", handleMouseMove);
            canvas.removeEventListener("mouseleave", handleMouseLeave);
        };
    }, [candles, zoom, crosshair, candleType]);

    return (
        <div className="bg-white rounded-lg shadow-sm p-6 h-[500px] relative">
            {/* 오버레이: 캔들 단위 버튼 (우측 상단) */}
            <div className="absolute top-2 right-2 z-10 flex space-x-2 bg-white bg-opacity-80 p-1 rounded">
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "seconds" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("seconds")}
                >
                    초봉
                </button>
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "minutes" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("minutes")}
                >
                    분봉
                </button>
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "days" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("days")}
                >
                    일봉
                </button>
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "weeks" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("weeks")}
                >
                    주봉
                </button>
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "months" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("months")}
                >
                    월봉
                </button>
                <button
                    className={`px-2 py-1 text-xs rounded ${candleType === "years" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800"}`}
                    onClick={() => setCandleType("years")}
                >
                    연봉
                </button>
            </div>

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
