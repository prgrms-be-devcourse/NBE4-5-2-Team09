"use client";

import { useParams } from "next/navigation";
import OrderbookList from "@/app/coin/components/OrderbookList";
import CandleChart from "@/app/coin/components/CandleChart";
import TradeList from "@/app/coin/components/TradeList";
import NewsList from "@/app/coin/components/NewsList";
import {
  generateMockOrderbook,
  generateMockTrades,
  generateMockCandles,
  generateMockNews,
} from "@/app/utils/mockData";
import { useWebSocket } from "@/app/context/WebSocketContext";

export default function ClientPage() {
  const { market } = useParams() as { market: string };
  const { ticker } = useWebSocket();

  // Mock data
  const orderbook = generateMockOrderbook();
  const trades = generateMockTrades();
  const candles = generateMockCandles();
  const news = generateMockNews();

  return (
    <div>
      <div className="mb-4">
        <h1 className="text-2xl font-bold flex items-center">
          <span className="mr-2">{market.split("-")[1]}</span>
          {ticker && (
            <span
              className={`text-xl ${
                ticker.change === "RISE"
                  ? "text-green-500"
                  : ticker.change === "FALL"
                  ? "text-red-500"
                  : "text-gray-500"
              }`}
            >
              ₩{ticker.tradePrice.toLocaleString()}
              <span className="ml-2 text-sm">
                {ticker.signedChangeRate > 0 ? "+" : ""}
                {(ticker.signedChangeRate * 100).toFixed(2)}%
              </span>
            </span>
          )}
        </h1>
      </div>

      <div className="bg-blue-50 py-4 mb-4 rounded-lg">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">24시간 거래량</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker
                  ? Math.floor(ticker.accTradeVolume24h).toLocaleString()
                  : "-"}
                <span className="text-sm">{market.split("-")[1]}</span>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일 종가</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker ? ticker.prevClosingPrice.toLocaleString() : "-"}
                <span className="text-sm">{market.split("-")[0]}</span>
              </div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">도미넌스</div>
              <div className="text-xl font-bold text-blue-600">52.1%</div>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <div className="text-sm text-gray-500">전일대비</div>
              <div className="text-xl font-bold text-blue-600">
                {ticker
                  ? (ticker.signedChangeRate * 100).toFixed(2) + "%"
                  : "-"}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        <div className="w-full">
          <CandleChart candles={candles} />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <TradeList trades={trades} />
          <OrderbookList
            orderbook={orderbook}
            currentPrice={ticker?.tradePrice || 0}
          />
        </div>

        <div className="w-full">
          <NewsList news={news} />
        </div>
      </div>
    </div>
  );
}
