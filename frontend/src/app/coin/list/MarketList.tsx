import { Card, CardContent } from "@/components/ui/card";
import { useWebSocket } from "@/context/WebSocketContext";
import { MarketDto } from "@/types";
import Link from "next/link";

export default function ClientPage({ markets }: { markets: MarketDto[] }) {
  const { tickers } = useWebSocket();

  return (
    <div className="grid grid-cols-3 gap-4 px-2 mt-5">
      {markets.map((market) => {
        // tickers에서 market.code에 해당하는 데이터 가져오기
        const ticker = tickers[market.code];

        // ticker가 null인 경우, 기본 값을 설정하거나 처리
        if (!ticker) {
          return (
            <Link key={market.code} href={`/coin/${market.code}`}>
              <Card
                key={market.code}
                className="bg-white shadow-sm rounded-sm border-0"
              >
                <CardContent className="p-4">
                  <div className="flex items-center space-x-1">
                    <h2 className="text-base font-bold">{market.koreanName}</h2>
                    <h3 className="text-sm text-gray-500">
                      {market.englishName}
                    </h3>
                  </div>
                  <div className="flex justify-between items-end mt-1">
                    <p className="text-xl font-semibold">
                      0{" "}
                      <span className="text-xs">
                        {market.code.split("-")[0]}
                      </span>
                    </p>
                    <p className="text-sm text-gray-500">0%</p>
                  </div>
                  <p className="text-xs text-gray-500 mt-2">거래량: 0</p>
                </CardContent>
              </Card>
            </Link>
          );
        }

        return (
          <Link key={market.code} href={`/coin/${market.code}`}>
            <Card
              key={market.code}
              className="bg-white shadow-sm rounded-sm border-0"
            >
              <CardContent className="p-4">
                <div className="flex items-center space-x-1">
                  <h2 className="text-base font-bold">{market.koreanName}</h2>
                  <h3 className="text-sm text-gray-500">
                    {market.englishName}
                  </h3>
                </div>
                <div className="flex justify-between items-end mt-1">
                  <p className="text-xl font-semibold">
                    {new Intl.NumberFormat(undefined, {
                      minimumFractionDigits:
                        ticker.tradePrice <= 1
                          ? 8
                          : ticker.tradePrice < 1000
                          ? 1
                          : 0,
                      maximumFractionDigits:
                        ticker.tradePrice <= 1
                          ? 8
                          : ticker.tradePrice < 1000
                          ? 1
                          : 0,
                    }).format(ticker.tradePrice)}{" "}
                    <span className="text-xs">{market.code.split("-")[0]}</span>
                  </p>
                  <p
                    className={
                      ticker.signedChangeRate >= 0
                        ? "text-sm text-red-500"
                        : "text-sm text-blue-500"
                    }
                  >
                    {ticker.signedChangeRate
                      ? `${ticker.signedChangeRate >= 0 ? "+" : ""}${(
                          ticker.signedChangeRate * 100
                        ).toFixed(2)}%`
                      : "0%"}
                  </p>
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  거래량:
                  {new Intl.NumberFormat().format(
                    parseFloat(ticker.accTradeVolume?.toFixed(3) || "0")
                  )}
                </p>
              </CardContent>
            </Card>
          </Link>
        );
      })}
    </div>
  );
}
