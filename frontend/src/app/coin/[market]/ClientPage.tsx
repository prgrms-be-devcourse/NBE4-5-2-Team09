'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { useWebSocket } from '@/context/WebSocketContext';
import OrderbookList from '../components/orderbook/OrderbookList';
import CandleChart from '../components/CandleChart';
import NewsList from '../components/NewsList';
import type { CandleItem, NewsItem } from '@/types';
import TradeList from '@/app/coin/components/TradeList';
import Ticker from '@/app/coin/components/Ticker';
import { fetchApi } from '@/lib/api';
import ChatPopup from '@/app/coin/components/ChatPopup'; // 채팅 모달 컴포넌트

export default function ClientPage() {
    const { market } = useParams() as { market: string };
    const { tickers, trades, orderbooks } = useWebSocket();
    const ticker = tickers?.[market] ?? null;
    const trade = trades?.[market] ?? null;
    const orderbook = orderbooks?.[market] ?? null;

    // 보정된 캔들 데이터를 저장
    const [candles, setCandles] = useState<CandleItem[]>([]);
    // 선택한 봉 단위: seconds, minutes, days, weeks, months, years
    const [candleType, setCandleType] = useState<
        'seconds' | 'minutes' | 'days' | 'weeks' | 'months' | 'years'
    >('seconds');
    // 분봉일 경우 단위 선택 (예: 1, 3, 5, 10, 15, 30, 60, 240)
    const [minuteUnit, setMinuteUnit] = useState(1);

    // 뉴스 데이터 state (NewsItem 배열)
    const [news, setNews] = useState<NewsItem[]>([]);

    // 채팅 팝업 표시 상태
    const [isChatPopupOpen, setChatPopupOpen] = useState(false);

    // 로그인 여부 상태: 세션 스토리지에 accessToken이 있으면 로그인된 것으로 판단
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // 봉 단위에 따른 폴링 간격(ms) 결정 함수
    const getPollingInterval = (type: string): number => {
        switch (type) {
            case 'seconds':
                return 1000;
            case 'minutes':
                return 30000;
            case 'days':
                return 3600000;
            case 'weeks':
            case 'months':
            case 'years':
                return 86400000;
            default:
                return 1000;
        }
    };

    // 캔들 데이터 fetch
    useEffect(() => {
        const fetchCandles = async () => {
            try {
                const unitQuery =
                    candleType === 'minutes' && minuteUnit ? `&unit=${minuteUnit}` : '';
                const data = await fetchApi<CandleItem[]>(
                    `/api/candle?market=${market}&candleType=${candleType}${unitQuery}`,
                    { method: 'GET' }
                );
                setCandles(data);
            } catch (err) {
                console.error('캔들 데이터 호출 오류:', err);
            }
        };

        // 봉 단위에 따라 동적으로 폴링 간격 결정
        const pollingInterval = getPollingInterval(candleType);
        fetchCandles();
        const interval = setInterval(fetchCandles, pollingInterval);
        return () => clearInterval(interval);
    }, [market, candleType, minuteUnit]);

    // 뉴스 데이터 fetch (API 라우트 통해 변환된 데이터 사용)
    useEffect(() => {
        const fetchNews = async () => {
            try {
                const url = `/api/news?market=${encodeURIComponent(market)}`;
                const data = await fetchApi<NewsItem[]>(url, { method: 'GET' });
                setNews(data);
            } catch (err) {
                console.error('뉴스 데이터 호출 오류:', err);
            }
        };

        if (market) {
            fetchNews();
        }
    }, [market]);

    // 로그인 여부 확인: 세션 스토리지의 accessToken을 체크
    useEffect(() => {
        const token = sessionStorage.getItem('accessToken');
        setIsLoggedIn(!!token);
    }, []);

    return (
        <div style={{position: 'relative'}}>
            {/* 우측 상단 채팅방 버튼 */}
            <button
                onClick={() =>
                    isLoggedIn
                        ? setChatPopupOpen(true)
                        : alert('로그인이 필요합니다.')
                }
                disabled={!isLoggedIn}
                className="absolute top-4 right-4 z-10 px-3 py-2 rounded-md transition-colors bg-primary text-primary-foreground dark:text-black dark:bg-primary-dark hover:bg-primary/90">
                채팅방(회원 전용)
            </button>

            <Ticker market={market} ticker={ticker}/>
            <div className="space-y-4">
                <CandleChart
                    candles={candles}
                    candleType={candleType}
                    setCandleType={setCandleType}
                    minuteUnit={minuteUnit}
                    setMinuteUnit={setMinuteUnit}
                />
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                    <TradeList market={market} trade={trade}/>
                    <OrderbookList market={market} orderbook={orderbook}/>
                </div>
                <div className="w-full">
                    <NewsList news={news}/>
                </div>
            </div>

            {/* 채팅 모달 팝업 */}
            {isChatPopupOpen && (
                <ChatPopup
                    marketCode={market}
                    onClose={() => setChatPopupOpen(false)}
                />
            )}
        </div>
    );
}
