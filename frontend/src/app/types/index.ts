export type TickerDto = {
  type: string; // 데이터 타입 (예: "ticker")
  code: string; // 마켓 코드 (예: "KRW-BTC")
  openingPrice: number; // 시가
  highPrice: number; // 고가
  lowPrice: number; // 저가
  tradePrice: number; // 현재가
  prevClosingPrice: number; // 전일 종가
  change: Change; // 전일 대비
  changePrice: number; // 전일 대비 값
  signedChangePrice: number; // 전일 대비 값 (부호 포함)
  changeRate: number; // 전일 대비 변동률
  signedChangeRate: number; // 전일 대비 변동률 (부호 포함)
  tradeVolume: number; // 가장 최근 거래량
  accTradeVolume: number; // 누적 거래량
  accTradeVolume24h: number; // 24시간 누적 거래량
  accTradePrice: number; // 누적 거래대금
  accTradePrice24h: number; // 24시간 누적 거래대금
  tradeDate: string; // 최근 거래 일자
  tradeTime: string; // 최근 거래 시각
  tradeTimestamp: number; // 체결 타임스탬프
  askBid: AskBid; // 매수/매도 구분
  accAskVolume: number; // 누적 매도량
  accBidVolume: number; // 누적 매수량
  highest52WeekPrice: number; // 52주 최고가
  highest52WeekDate: string; // 52주 최고가 달성일
  lowest52WeekPrice: number; // 52주 최저가
  lowest52WeekDate: string; // 52주 최저가 달성일
  marketState: MarketState; // 거래 상태
  marketWarning: MarketWarning; // 유의 종목 여부
  timestamp: number; // 타임스탬프
};

export enum Change {
  RISE = "RISE", // 상승
  EVEN = "EVEN", // 보합
  FALL = "FALL", // 하락
}

export enum MarketState {
  PREVIEW = "PREVIEW", // 입금지원
  ACTIVE = "ACTIVE", // 거래지원 가능
  DELISTED = "DELISTED", // 거래지원 종료
}

export enum MarketWarning {
  NONE = "NONE", // 유의 종목 아님
  CAUTION = "CAUTION", // 유의 종목
}

export enum AskBid {
  ASK = "ASK", // 매도
  BID = "BID", // 매수
}

export type OrderBookItem = {
  price: number;
  quantity: number;
  total: number;
};

export type OrderBook = {
  asks: OrderBookItem[];
  bids: OrderBookItem[];
};

export type TradeItem = {
  id: string;
  price: number;
  quantity: number;
  side: AskBid;
  timestamp: number;
};

export type CandleItem = {
  time: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
};

export type NewsItem = {
  id: string;
  title: string;
  summary: string;
  source: string;
  publishedAt: string;
  url: string;
};
