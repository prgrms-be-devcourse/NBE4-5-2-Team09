'use client';
import { AutoTextSize } from 'auto-text-size';
import type { OrderbookDto } from '@/types';

interface OrderbookChartProps {
  orderbook: OrderbookDto;
  isTotalMode: boolean;
  quote: string;
  base: string;
  formatPrice: (price: number) => string;
  formatQuantity: (quantity: number) => string;
  maxAskValue: number;
  maxBidValue: number;
  setContainerRef: (node: HTMLDivElement | null) => void;
}

export function OrderbookChart({
  orderbook,
  isTotalMode,
  quote,
  base,
  formatPrice,
  formatQuantity,
  maxAskValue,
  maxBidValue,
  setContainerRef,
}: OrderbookChartProps) {
  return (
    <div className="overflow-y-auto max-h-[500px]" ref={setContainerRef}>
      <table className="w-full table-fixed">
        <thead className="bg-muted sticky top-0">
          <tr className="text-muted-foreground text-xs">
            <th className="px-4 py-2 text-right w-1/3">
              {isTotalMode ? '매도 총액' : '매도 잔량'}
            </th>
            <th className="px-4 py-2 text-center w-1/3">가격 ({quote})</th>
            <th className="px-4 py-2 text-left w-1/3">{isTotalMode ? '매수 총액' : '매수 잔량'}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-muted">
          {orderbook.orderbookUnits.map(({ askPrice, askSize }, index) => {
            const askDisplayValue = isTotalMode ? askPrice * askSize : askSize;
            return (
              <tr key={`ask-${index}`} className="border-b border-muted">
                <td className="px-4 py-2 text-sm">
                  <div
                    className="bg-light-red h-6 flex items-center justify-end pr-2 ml-auto text-xs"
                    style={{
                      width: `${maxAskValue ? (askDisplayValue / maxAskValue) * 100 : 0}%`,
                      minWidth: '2px',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {isTotalMode ? formatPrice(askDisplayValue) : formatQuantity(askDisplayValue)}
                  </div>
                </td>
                <td className="px-4 py-2 text-sm text-center text-red-500">
                  {formatPrice(askPrice)}
                </td>
                {index === 0 && (
                  <td rowSpan={orderbook.orderbookUnits.length} className="px-1 py-2 align-bottom">
                    <div className="flex flex-col w-full gap-1 text-muted-foreground text-xs">
                      {[
                        {
                          label: '중간 가격:',
                          value: `${formatPrice(orderbook.midPrice)} ${quote}`,
                        },
                        {
                          label: '스프레드:',
                          value: `${formatPrice(orderbook.spread)} ${quote}`,
                        },
                        {
                          label: '유동성:',
                          value: `${orderbook.liquidityDepth.toFixed(2)}%`,
                        },
                        {
                          label: '불균형:',
                          value: `${orderbook.imbalance.toFixed(2)}%`,
                          valueClass:
                            orderbook.imbalance > 0
                              ? 'text-blue-500'
                              : orderbook.imbalance < 0
                                ? 'text-red-500'
                                : 'text-muted-foreground',
                        },
                      ].map((item, idx) => (
                        <div key={`ask-box-${idx}`} className="w-full">
                          <AutoTextSize className="w-full">
                            <div className="flex justify-between w-full pr-2">
                              <div className="font-medium text-left">{item.label}</div>
                              <div className={`text-right ${item.valueClass || ''}`}>
                                {item.value}
                              </div>
                            </div>
                          </AutoTextSize>
                        </div>
                      ))}
                    </div>
                  </td>
                )}
              </tr>
            );
          })}

          {orderbook.orderbookUnits.map(({ bidPrice, bidSize }, index) => {
            const bidDisplayValue = isTotalMode ? bidPrice * bidSize : bidSize;
            return (
              <tr key={`bid-${index}`} className="border-b border-muted">
                {index === 0 && (
                  <td rowSpan={orderbook.orderbookUnits.length} className="px-1 py-2 align-top">
                    <div className="flex flex-col w-full gap-1 text-muted-foreground text-xs">
                      {[
                        {
                          label: '전체 매도 잔량:',
                          value: `${formatPrice(orderbook.totalAskSize)} ${base}`,
                        },
                        {
                          label: '전체 매수 잔량:',
                          value: `${formatPrice(orderbook.totalBidSize)} ${base}`,
                        },
                      ].map((item, idx) => (
                        <div key={`bid-box-${idx}`} className="w-full">
                          <AutoTextSize className="w-full">
                            <div className="flex justify-between w-full pl-2">
                              <div className="font-medium text-left">{item.label}</div>
                              <div className="text-right">{item.value}</div>
                            </div>
                          </AutoTextSize>
                        </div>
                      ))}
                    </div>
                  </td>
                )}
                <td className="px-4 py-2 text-sm text-center text-blue-500 ">
                  {formatPrice(bidPrice)}
                </td>
                <td className="px-4 py-2 text-sm">
                  <div
                    className="bg-light-blue h-6 flex items-center text-xs px-2 mr-2"
                    style={{
                      width: `${maxBidValue ? (bidDisplayValue / maxBidValue) * 100 : 0}%`,
                      minWidth: '2px',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {isTotalMode ? formatPrice(bidDisplayValue) : formatQuantity(bidDisplayValue)}
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
