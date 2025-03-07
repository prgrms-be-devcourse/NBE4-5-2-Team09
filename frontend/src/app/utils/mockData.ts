import {
  type Orderbook,
  type OrderbookItem,
  type TradeItem,
  type CandleItem,
  type NewsItem,
  AskBid,
} from "@/app/types";

// 모의 호가(Orderbook) 생성 함수
export function generateMockOrderbook(): Orderbook {
  const basePrice = 799; // 현재 EOS 가격
  const asks: OrderbookItem[] = []; // 매도 주문 리스트
  const bids: OrderbookItem[] = []; // 매수 주문 리스트

  // 매도 주문 생성 (기준 가격보다 높은 가격)
  for (let i = 0; i < 5; i++) {
    const price = basePrice + (i + 1) * 10000;
    const quantity = Number.parseFloat((Math.random() * 2 + 0.1).toFixed(4));
    asks.push({
      price,
      quantity,
      total: price * quantity,
    });
  }

  // 매수 주문 생성 (기준 가격보다 낮은 가격)
  for (let i = 0; i < 5; i++) {
    const price = basePrice - (i + 1) * 10000;
    const quantity = Number.parseFloat((Math.random() * 2 + 0.1).toFixed(4));
    bids.push({
      price,
      quantity,
      total: price * quantity,
    });
  }

  // 매도 주문은 가격 오름차순 정렬, 매수 주문은 가격 내림차순 정렬
  asks.sort((a, b) => a.price - b.price);
  bids.sort((a, b) => b.price - a.price);

  return { asks, bids };
}

// 모의 체결 내역(TradeItem) 생성 함수
export function generateMockTrades(): TradeItem[] {
  const basePrice = 799; // 현재 EOS 가격
  const trades: TradeItem[] = [];
  const now = Date.now(); // 현재 시간

  for (let i = 0; i < 20; i++) {
    const priceChange = Math.floor(Math.random() * 50) - 25;
    const price = basePrice + priceChange;
    const quantity = Number.parseFloat((Math.random() * 10 + 0.01).toFixed(5));
    const side = Math.random() > 0.5 ? AskBid.ASK : AskBid.BID; // 매도/매수 랜덤 지정

    trades.push({
      id: `trade-${i}`,
      price,
      quantity,
      side,
      timestamp: now - i * 10000, // 10초 간격으로 시간 설정
    });
  }

  return trades;
}

// 모의 캔들 차트(CandleItem) 생성 함수
export function generateMockCandles(): CandleItem[] {
  const basePrice = 799; // 현재 EOS 가격
  const candles: CandleItem[] = [];
  const now = Date.now();
  const dayInMs = 24 * 60 * 60 * 1000; // 하루(밀리초 단위)

  for (let i = 30; i >= 0; i--) {
    const time = now - i * dayInMs;
    const volatility = basePrice * 0.05; // 5% 변동성 적용

    const open = basePrice + (Math.random() - 0.5) * volatility;
    const close = basePrice + (Math.random() - 0.5) * volatility;
    const high = Math.max(open, close) + Math.random() * volatility * 0.5;
    const low = Math.min(open, close) - Math.random() * volatility * 0.5;
    const volume = Math.random() * 1000 + 100;

    candles.push({
      time,
      open,
      high,
      low,
      close,
      volume,
    });
  }

  return candles;
}

// 모의 뉴스 데이터(NewsItem) 생성 함수
export function generateMockNews(): NewsItem[] {
  const news: NewsItem[] = [
    {
      id: "1",
      title: "비트코인, 5만달러 돌파 후 속락세",
      summary:
        "2024.03.15 14:30 - 비트코인이 5만 달러를 돌파한 후 급격한 하락세를 보이고 있습니다. 전문가들은 단기 조정 후 상승세를 예상합니다.",
      source: "코인뉴스",
      publishedAt: "2024-03-15T14:30:00Z",
      url: "#",
    },
    {
      id: "2",
      title: "SEC, 비트코인 ETF 추가 승인 검토 중",
      summary:
        "2024.03.15 13:45 - 미국 증권거래위원회(SEC)가 추가적인 비트코인 ETF 승인을 검토 중인 것으로 알려졌습니다.",
      source: "글로벌경제",
      publishedAt: "2024-03-15T13:45:00Z",
      url: "#",
    },
    {
      id: "3",
      title: "가상자산 거래소, 신규 보안 정책 도입 예정",
      summary:
        "2024.03.15 11:20 - 주요 가상자산 거래소들이 해킹 방지를 위한 새로운 보안 정책을 도입할 예정입니다.",
      source: "테크뉴스",
      publishedAt: "2024-03-15T11:20:00Z",
      url: "#",
    },
    {
      id: "4",
      title: "이더리움 2.0 업그레이드, 예상보다 빠른 진행",
      summary:
        "이더리움 재단은 이더리움 2.0 업그레이드가 예상보다 빠르게 진행되고 있다고 발표했습니다.",
      source: "블록체인투데이",
      publishedAt: "2024-03-15T10:15:00Z",
      url: "#",
    },
    {
      id: "5",
      title: "중앙은행들, CBDC 개발 가속화",
      summary:
        "전 세계 중앙은행들이 중앙은행 디지털 화폐(CBDC) 개발을 가속화하고 있습니다.",
      source: "경제일보",
      publishedAt: "2024-03-15T09:30:00Z",
      url: "#",
    },
  ];

  return news;
}
