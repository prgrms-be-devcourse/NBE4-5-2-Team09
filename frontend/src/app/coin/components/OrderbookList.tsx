"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import type { OrderbookDto } from "@/types";
import { useWebSocket } from "@/context/WebSocketContext";
import { Skeleton } from "@/components/ui/skeleton";
import { Card } from "@/components/ui/card";
import { Textfit } from "react-textfit";
import { Switch } from "@/components/ui/switch";
import { Label } from "@/components/ui/label";

interface OrderBookListProps {
  market: string;
}

export default function OrderbookList({ market }: OrderBookListProps) {
  const [quote, base] = market.split("-");
  const { orderbooks } = useWebSocket();
  const orderbook: OrderbookDto | null = orderbooks[market] || null;
  const userScrolled = useRef(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isTotalMode, setIsTotalMode] = useState(false);

  const handleToggleMode = () => {
    setIsTotalMode((prev) => !prev);
  };

  const setContainerRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (node) {
        containerRef.current = node;
        node.onscroll = () => {
          userScrolled.current = true;
        };
        if (orderbook && !userScrolled.current) {
          node.scrollTo({
            top: node.scrollHeight / 2 - node.clientHeight / 2,
            behavior: "auto",
          });
        }
      }
    },
    [orderbook]
  );
  const formatPrice = (price: number) => price.toLocaleString();
  const formatQuantity = (quantity: number) => quantity.toFixed(4);

  if (!orderbook) return <Skeleton className="h-96 w-full rounded-md" />;

  const maxAskValue = isTotalMode
    ? Math.max(
        ...orderbook.orderbookUnits.map((u) => u.askPrice * u.askSize),
        0
      )
    : Math.max(...orderbook.orderbookUnits.map((u) => u.askSize), 0);
  const maxBidValue = isTotalMode
    ? Math.max(
        ...orderbook.orderbookUnits.map((u) => u.bidPrice * u.bidSize),
        0
      )
    : Math.max(...orderbook.orderbookUnits.map((u) => u.bidSize), 0);

  return (
    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-gray-200 flex justify-between items-center">
        <h2 className="text-lg font-semibold">호가 정보</h2>
        <div className="flex items-center gap-2">
          <Label htmlFor="toggle-switch">
            {isTotalMode ? `총액(${quote})` : `수량(${base})`}
          </Label>
          <Switch
            id="toggle-switch"
            checked={isTotalMode}
            onCheckedChange={handleToggleMode}
            className="w-10 h-6 data-[state=checked]:bg-gray-500 data-[state=unchecked]:bg-gray-300"
          />
        </div>
      </div>

      <div className="overflow-y-auto max-h-[500px]" ref={setContainerRef}>
        <table className="w-full table-fixed">
          <thead className="bg-gray-50 sticky top-0">
            <tr className="text-gray-500 text-xs">
              <th className="px-4 py-2 text-right w-1/3">
                {isTotalMode ? "매도 총액" : "매도 잔량"}
              </th>
              <th className="px-4 py-2 text-center w-1/3">가격 ({quote})</th>
              <th className="px-4 py-2 text-left w-1/3">
                {isTotalMode ? "매수 총액" : "매수 잔량"}
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-gray-100">
            {orderbook?.orderbookUnits.map(({ askPrice, askSize }, index) => {
              const askDisplayValue = isTotalMode
                ? askPrice * askSize
                : askSize;

              return (
                <tr key={`ask-${index}`} className="border-b border-gray-100">
                  <td className="px-4 py-2 text-sm">
                    <div
                      className="h-6 flex items-center justify-end pr-2 ml-auto text-xs"
                      style={{
                        width: `${
                          maxAskValue
                            ? (askDisplayValue / maxAskValue) * 100
                            : 0
                        }%`,
                        backgroundColor: "rgb(254, 202, 202)",
                        minWidth: "2px",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {isTotalMode
                        ? formatPrice(askDisplayValue)
                        : formatQuantity(askDisplayValue)}
                    </div>
                  </td>
                  <td className="px-4 py-2 text-sm text-center text-red-500">
                    {formatPrice(askPrice)}
                  </td>
                  {index === 0 && (
                    <td
                      rowSpan={orderbook.orderbookUnits.length}
                      className="px-1 py-2 align-bottom"
                    >
                      <div className="flex flex-col w-full gap-1 text-gray-500 text-xs">
                        {[
                          {
                            label: "중간 가격:",
                            value: `${formatPrice(
                              orderbook.midPrice
                            )} ${quote}`,
                          },
                          {
                            label: "스프레드:",
                            value: `${formatPrice(orderbook.spread)} ${quote}`,
                          },
                          {
                            label: "유동성:",
                            value: `${orderbook.liquidityDepth.toFixed(2)}%`,
                          },
                          {
                            label: "불균형:",
                            value: `${orderbook.imbalance.toFixed(2)}%`,
                            valueClass:
                              orderbook.imbalance > 0
                                ? "text-blue-500"
                                : orderbook.imbalance < 0
                                ? "text-red-500"
                                : "text-gray-500",
                          },
                        ].map((item, idx) => (
                          <Textfit key={idx} mode="single" max={12}>
                            <div className="flex justify-between w-full pr-2">
                              <div className="font-medium text-left">
                                {item.label}
                              </div>
                              <div
                                className={`text-right ${
                                  item.valueClass || ""
                                }`}
                              >
                                {item.value}
                              </div>
                            </div>
                          </Textfit>
                        ))}
                      </div>
                    </td>
                  )}
                </tr>
              );
            })}

            {orderbook?.orderbookUnits.map(({ bidPrice, bidSize }, index) => {
              const bidDisplayValue = isTotalMode
                ? bidPrice * bidSize
                : bidSize;

              return (
                <tr key={`bid-${index}`} className="border-b border-gray-100">
                  {index === 0 && (
                    <td
                      rowSpan={orderbook.orderbookUnits.length}
                      className="px-1 py-2 align-top"
                    >
                      <div className="flex flex-col w-full gap-1 text-gray-500 text-xs">
                        {[
                          {
                            label: "전체 매도 잔량:",
                            value: `${formatPrice(
                              orderbook.totalAskSize
                            )} ${base}`,
                          },
                          {
                            label: "전체 매수 잔량:",
                            value: `${formatPrice(
                              orderbook.totalBidSize
                            )} ${base}`,
                          },
                        ].map((item, idx) => (
                          <Textfit key={idx} mode="single" max={12}>
                            <div className="flex justify-between w-full pl-2">
                              <div className="font-medium text-left">
                                {item.label}
                              </div>
                              <div className="text-right">{item.value}</div>
                            </div>
                          </Textfit>
                        ))}
                      </div>
                    </td>
                  )}
                  <td className="px-4 py-2 text-sm text-center text-blue-500 ">
                    {formatPrice(bidPrice)}
                  </td>
                  <td className="px-4 py-2 text-sm">
                    <div
                      className="h-6 flex items-center text-xs px-2 mr-2"
                      style={{
                        width: `${
                          maxBidValue
                            ? (bidDisplayValue / maxBidValue) * 100
                            : 0
                        }%`,
                        backgroundColor: "rgb(191, 219, 254)",
                        minWidth: "2px",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {isTotalMode
                        ? formatPrice(bidDisplayValue)
                        : formatQuantity(bidDisplayValue)}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
