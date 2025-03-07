/* "use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Client } from "@stomp/stompjs"; // Stomp.js import
import type { MarketDto } from "../types";
import type { TickerDto } from "../types";
import { SelectGroup } from "@radix-ui/react-select";

export default function MarketListPage() {
    const [markets, setMarkets] = useState<MarketDto[]>([]);
    const [ticker, setTicker] = useState<Record<string, TickerDto>>({});
    const [page, setPage] = useState(0);
    const [itemsPerPage, setItemsPerPage] = useState(8);
    const [sortOrder, setSortOrder] = useState("asc");
    const [isConnected, setIsConnected] = useState(false);
    const [connectionError, setConnectionError] = useState<string | undefined>(undefined);
    const [quote, setQuote] = useState("all"); // 필터 상태 관리 (전체/KRW/BTC/USDT)
    const [totalElements, setTotalElements] = useState(0); // 총 데이터 개수
    const [totalPages, setTotalPages] = useState(0); // 총 페이지 수

    // REST API로 마켓 목록 가져오기 (기준 통화에 따라 필터링)
    useEffect(() => {
        async function fetchMarkets() {
            let url = "http://localhost:8080/api/market"; // 기본 URL
            if (quote !== "all") {
                url = `http://localhost:8080/api/market/quote?type=${quote}&page=${page}`;
            } else {
                url = `http://localhost:8080/api/market?page=${page}`; // quote가 "all"일 때는 이렇게 설정
            }

            const response = await fetch(url);
            const data = await response.json();
            setMarkets(data.content); // 서버에서 받아온 마켓 목록
            setTotalElements(data.totalElements); // 전체 데이터 개수
            setTotalPages(data.totalPages); // 전체 페이지 수
        }

        fetchMarkets();
    }, [quote, page, itemsPerPage]); // quote 필터가 변경될 때마다 호출

    // 웹소켓으로 실시간 가격 정보 받기
    useEffect(() => {
        const client = new Client({
            brokerURL: "ws://localhost:8080/websocket",
            debug: (str) => {
                console.log(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            setIsConnected(true);
            setConnectionError(undefined);

            client.subscribe("/sub/coin/ticker", (message) => {
                try {
                    const tickerData = JSON.parse(message.body);
                    setTicker((prevData) => ({
                        ...prevData,
                        [tickerData.code]: {
                            tradePrice: tickerData.tradePrice ?? prevData[tickerData.code]?.tradePrice ?? 0,
                            signedChangeRate: tickerData.signedChangeRate !== undefined
                                ? tickerData.signedChangeRate * 100
                                : prevData[tickerData.code]?.signedChangeRate ?? 0,
                            accTradeVolume: tickerData.accTradeVolume ?? prevData[tickerData.code]?.accTradeVolume ?? 0,
                        },
                    }));
                } catch (error) {
                    console.error("Failed to parse ticker data:", error);
                }
            });
        };

        client.onStompError = (frame) => {
            setConnectionError(`STOMP error: ${frame.headers.message}`);
            setIsConnected(false);
        };

        client.onWebSocketError = (event) => {
            setConnectionError("WebSocket connection error");
            setIsConnected(false);
        };

        client.activate();

        return () => {
            if (client.active) {
                client.deactivate();
            }
        };
    }, []);

    // 데이터 정렬
    const sortedMarkets = [...markets].sort((a, b) => {
        if (sortOrder === "accTradeVolumeAsc") {
            return (ticker[b.code]?.accTradeVolume ?? 0) - (ticker[a.code]?.accTradeVolume ?? 0); // 거래량 순
        }
        if (sortOrder === "accTradeVolumeDesc") {
            return (ticker[a.code]?.accTradeVolume ?? 0) - (ticker[b.code]?.accTradeVolume ?? 0);
        }
        if (sortOrder === "tradePriceAsc") {
            return (ticker[b.code]?.tradePrice ?? 0) - (ticker[a.code]?.tradePrice ?? 0);
        }
        if (sortOrder === "tradePriceDesc") {
            return (ticker[a.code]?.tradePrice ?? 0) - (ticker[b.code]?.tradePrice ?? 0);
        }
        return sortOrder === "asc" // 마켓 코드 순
            ? a.code.localeCompare(b.code)
            : b.code.localeCompare(a.code);
    });

    const paginatedData = sortedMarkets.slice(0, itemsPerPage);

    // 탭 변경 시 페이지 번호 초기화
    const handleTabChange = (value: string) => {
        setQuote(value);
        setPage(0); // 페이지를 초기화
    };

    // 정렬 변경 시 페이지 번호 유지
    const handleSortChange = (value: string) => {
        setSortOrder(value);
    };

    return (
        <div className="p-6">

            // 기준 통화 필터 옵션 
            <Tabs value={quote} onValueChange={handleTabChange}>
                <TabsList className="grid w-full grid-cols-4 bg-gray-100 mb-4">
                    <TabsTrigger value="all">전체</TabsTrigger>
                    <TabsTrigger value="KRW">KRW</TabsTrigger>
                    <TabsTrigger value="BTC">BTC</TabsTrigger>
                    <TabsTrigger value="USDT">USDT</TabsTrigger>
                </TabsList>
            </Tabs>

            // 정렬 옵션
            <Select onValueChange={handleSortChange} defaultValue="asc">
                <SelectTrigger className="w-[180px] mb-4 shadow-sm rounded-sm border-0 ml-auto">
                    <SelectValue placeholder="정렬" />
                </SelectTrigger>
                <SelectContent className="bg-white shadow-sm rounded-sm border-0">
                    <SelectGroup>
                        <SelectItem value="asc">마켓 코드 오름차순</SelectItem>
                        <SelectItem value="desc">마켓 코드 내림차순</SelectItem>
                        <SelectItem value="accTradeVolumeAsc">거래량 많은 순</SelectItem>
                        <SelectItem value="accTradeVolumeDesc">거래량 적은 순</SelectItem>
                        <SelectItem value="tradePriceAsc">현재가 높은 순</SelectItem>
                        <SelectItem value="tradePriceDesc">현재가 낮은 순</SelectItem>
                    </SelectGroup>
                </SelectContent>
            </Select>

            // 코인 카드 리스트 
            <div className="grid grid-cols-4 gap-4">
                {paginatedData.map((coin) => (
                    <Card key={coin.code} className="bg-white shadow-sm rounded-sm border-0">
                        <CardContent className="p-4">
                            <div className="flex items-center space-x-1">
                                <h2 className="text-base font-bold">{coin.koreanName}</h2>
                                <h3 className="text-sm text-gray-500">{coin.englishName}</h3>
                            </div>
                            <div className="flex justify-between items-center mt-1">
                                <p className="text-xl font-semibold">
                                    {new Intl.NumberFormat().format(parseFloat(ticker[coin.code]?.tradePrice?.toFixed(4)) || 0)}{" "}
                                    <span className="text-xs">{coin.code.split('-')[0]}</span>
                                </p>
                                <p className={ticker[coin.code]?.signedChangeRate >= 0 ? "text-sm text-red-500" : "text-sm text-blue-500"}>
                                    {ticker[coin.code]?.signedChangeRate?.toFixed(2) || "정보 없음"}%
                                </p>
                            </div>
                            <p className="text-xs text-gray-500 mt-2">
                                거래량: {new Intl.NumberFormat().format(parseFloat(ticker[coin.code]?.accTradeVolume?.toFixed(3) || '0'))}
                            </p>
                        </CardContent>
                    </Card>
                ))}
            </div>

            // 페이지네이션 
            <div className="flex justify-center items-center mt-6 space-x-2">
                <Button onClick={() => setPage((p) => Math.max(p - 1, 0))} disabled={page === 0}>
                    이전
                </Button>
                <span className="text-sm">
                    {page + 1} / {totalPages}
                </span>
                <Button onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))} disabled={page === totalPages - 1}>
                    다음
                </Button>
            </div>

        </div>
    );
}
*/

"use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Client } from "@stomp/stompjs"; // Stomp.js import
import type { MarketDto } from "../types";
import type { TickerDto } from "../types";
import { SelectGroup } from "@radix-ui/react-select";

export default function MarketListPage() {
    const [markets, setMarkets] = useState<MarketDto[]>([]);
    const [ticker, setTicker] = useState<Record<string, TickerDto>>({});
    const [page, setPage] = useState(0);
    const [itemsPerPage, setItemsPerPage] = useState(10);
    const [sortOrder, setSortOrder] = useState("asc");
    const [isConnected, setIsConnected] = useState(false);
    const [connectionError, setConnectionError] = useState<string | undefined>(undefined);
    const [quote, setQuote] = useState("all"); // 필터 상태 관리 (전체/KRW/BTC/USDT)
    const [totalElements, setTotalElements] = useState(0); // 총 데이터 개수
    const [totalPages, setTotalPages] = useState(0); // 총 페이지 수

    // REST API로 마켓 목록 가져오기 (기준 통화에 따라 필터링)
    useEffect(() => {
        async function fetchMarkets() {
            let url = "http://localhost:8080/api/market"; // 기본 URL
            if (quote !== "all") {
                url = `http://localhost:8080/api/market/quote?type=${quote}&page=${page}&size=9`;
            } else {
                url = `http://localhost:8080/api/market?page=${page}&size=9`; // quote가 "all"일 때는 이렇게 설정
            }

            const response = await fetch(url);
            const data = await response.json();
            setMarkets(data.content); // 서버에서 받아온 마켓 목록
            setTotalElements(data.totalElements); // 전체 데이터 개수
            setTotalPages(data.totalPages); // 전체 페이지 수
        }

        fetchMarkets();
    }, [quote, page, itemsPerPage]); // quote 필터가 변경될 때마다 호출

    // 웹소켓으로 실시간 가격 정보 받기
    useEffect(() => {
        const client = new Client({
            brokerURL: "ws://localhost:8080/websocket",
            debug: (str) => {
                console.log(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
            setIsConnected(true);
            setConnectionError(undefined);

            client.subscribe("/sub/coin/ticker", (message) => {
                try {
                    const tickerData = JSON.parse(message.body);
                    setTicker((prevData) => ({
                        ...prevData,
                        [tickerData.code]: {
                            tradePrice: tickerData.tradePrice ?? prevData[tickerData.code]?.tradePrice ?? 0,
                            signedChangeRate: tickerData.signedChangeRate !== undefined
                                ? tickerData.signedChangeRate * 100
                                : prevData[tickerData.code]?.signedChangeRate ?? 0,
                            accTradeVolume: tickerData.accTradeVolume ?? prevData[tickerData.code]?.accTradeVolume ?? 0,
                        },
                    }));
                } catch (error) {
                    console.error("Failed to parse ticker data:", error);
                }
            });
        };

        client.onStompError = (frame) => {
            setConnectionError(`STOMP error: ${frame.headers.message}`);
            setIsConnected(false);
        };

        client.onWebSocketError = (event) => {
            setConnectionError("WebSocket connection error");
            setIsConnected(false);
        };

        client.activate();

        return () => {
            if (client.active) {
                client.deactivate();
            }
        };
    }, []);

    // 데이터 정렬
    const sortedMarkets = [...markets].sort((a, b) => {
        if (sortOrder === "accTradeVolumeAsc") {
            return (ticker[b.code]?.accTradeVolume ?? 0) - (ticker[a.code]?.accTradeVolume ?? 0); // 거래량 순
        }
        if (sortOrder === "accTradeVolumeDesc") {
            return (ticker[a.code]?.accTradeVolume ?? 0) - (ticker[b.code]?.accTradeVolume ?? 0);
        }
        if (sortOrder === "tradePriceAsc") {
            return (ticker[b.code]?.tradePrice ?? 0) - (ticker[a.code]?.tradePrice ?? 0);
        }
        if (sortOrder === "tradePriceDesc") {
            return (ticker[a.code]?.tradePrice ?? 0) - (ticker[b.code]?.tradePrice ?? 0);
        }
        return sortOrder === "asc" // 마켓 코드 순
            ? a.code.localeCompare(b.code)
            : b.code.localeCompare(a.code);
    });

    const paginatedData = sortedMarkets.slice(0, itemsPerPage);

    // 필터 변경 시 페이지 번호를 0으로 설정
    const handleQuoteChange = (newQuote: string) => {
        setQuote(newQuote);
        setPage(0); // 페이지를 0으로 리셋
    };

    return (
        <div className="p-6">

            {/* 기준 통화 필터 옵션 */}
            <Tabs value={quote} onValueChange={handleQuoteChange}>
                <TabsList className="grid w-full grid-cols-4 bg-gray-100 mb-4">
                    <TabsTrigger value="all">전체</TabsTrigger>
                    <TabsTrigger value="KRW">KRW</TabsTrigger>
                    <TabsTrigger value="BTC">BTC</TabsTrigger>
                    <TabsTrigger value="USDT">USDT</TabsTrigger>
                </TabsList>
            </Tabs>

            {/* 정렬 옵션*/}
            <Select onValueChange={setSortOrder} defaultValue="asc">
                <SelectTrigger className="w-[180px] mb-4 shadow-sm rounded-sm border-0 ml-auto">
                    <SelectValue placeholder="정렬" />
                </SelectTrigger>
                <SelectContent className="bg-white shadow-sm rounded-sm border-0">
                    <SelectGroup>
                        <SelectItem value="asc">마켓 코드 오름차순</SelectItem>
                        <SelectItem value="desc">마켓 코드 내림차순</SelectItem>
                        <SelectItem value="accTradeVolumeAsc">거래량 많은 순</SelectItem>
                        <SelectItem value="accTradeVolumeDesc">거래량 적은 순</SelectItem>
                        <SelectItem value="tradePriceAsc">현재가 높은 순</SelectItem>
                        <SelectItem value="tradePriceDesc">현재가 낮은 순</SelectItem>
                    </SelectGroup>
                </SelectContent>
            </Select>

            {/* 코인 카드 리스트 */}
            <div className="grid grid-cols-3 gap-4">
                {paginatedData.map((coin) => (
                    <Card key={coin.code} className="bg-white shadow-sm rounded-sm border-0">
                        <CardContent className="p-4">
                            <div className="flex items-center space-x-1">
                                <h2 className="text-base font-bold">{coin.koreanName}</h2>
                                <h3 className="text-sm text-gray-500">{coin.englishName}</h3>
                            </div>
                            <div className="flex justify-between items-end mt-1">
                                <p className="text-xl font-semibold">
                                    {new Intl.NumberFormat().format(parseFloat(ticker[coin.code]?.tradePrice?.toFixed(4)) || 0)}{" "}
                                    <span className="text-xs">{coin.code.split('-')[0]}</span>
                                </p>
                                <p className={ticker[coin.code]?.signedChangeRate >= 0 ? "text-sm text-red-500" : "text-sm text-blue-500"}>
                                    {ticker[coin.code]?.signedChangeRate?.toFixed(2) || 0}%
                                </p>
                            </div>
                            <p className="text-xs text-gray-500 mt-2">
                                거래량: {new Intl.NumberFormat().format(parseFloat(ticker[coin.code]?.accTradeVolume?.toFixed(3) || '0'))}
                            </p>
                        </CardContent>
                    </Card>
                ))}
            </div>

            {/* 페이지네이션 */}
            <div className="flex justify-center items-center mt-6 space-x-2">
                <Button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
                    이전
                </Button>
                <span>{page + 1} / {totalPages + 1}</span>
                <Button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
                    다음
                </Button>
            </div>
        </div>
    );
}
