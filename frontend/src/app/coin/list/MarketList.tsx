import { useWebSocket } from "@/app/context/WebSocketContext";
import { MarketDto } from "@/app/types";
import { Card, CardContent } from "@/components/ui/card";
import Link from "next/link";

export default function ClientPage({ markets }: { markets: MarketDto[] }) {
    const { tickers } = useWebSocket();

    return (
        <div className="grid grid-cols-3 gap-4 px-2 mt-5">
            {markets.map((market) => (
                <Link key={market.code} href={`/coin/${market.code}`}>
                    <Card key={market.code} className="bg-white shadow-sm rounded-sm border-0">
                        <CardContent className="p-4">
                            <div className="flex items-center space-x-1">
                                <h2 className="text-base font-bold">{market.koreanName}</h2>
                                <h3 className="text-sm text-gray-500">{market.englishName}</h3>
                            </div>
                            <div className="flex justify-between items-end mt-1">
                                <p className="text-xl font-semibold">
                                    {new Intl.NumberFormat(undefined, {
                                        minimumFractionDigits: tickers[market.code]?.tradePrice
                                            ? tickers[market.code]?.tradePrice <= 1
                                                ? 8
                                                : tickers[market.code]?.tradePrice < 1000
                                                    ? 1
                                                    : 0
                                            : 0,
                                        maximumFractionDigits: tickers[market.code]?.tradePrice
                                            ? tickers[market.code]?.tradePrice <= 1
                                                ? 8
                                                : tickers[market.code]?.tradePrice < 1000
                                                    ? 1
                                                    : 0
                                            : 0,
                                    }).format(tickers[market.code]?.tradePrice ?? 0)}{" "}
                                    <span className="text-xs">{market.code.split("-")[0]}</span>
                                </p>
                                <p className={tickers[market.code]?.signedChangeRate >= 0 ? "text-sm text-red-500" : "text-sm text-blue-500"}>
                                    {tickers[market.code]?.signedChangeRate
                                        ? `${tickers[market.code]?.signedChangeRate >= 0 ? "+" : ""}${(tickers[market.code]?.signedChangeRate * 100).toFixed(2)}%`
                                        : "0%"}
                                </p>
                            </div>
                            <p className="text-xs text-gray-500 mt-2">
                                거래량: {new Intl.NumberFormat().format(parseFloat(tickers[market.code]?.accTradeVolume?.toFixed(3) || '0'))}
                            </p>
                        </CardContent>
                    </Card>
                </Link>
            ))}
        </div>
    );
}