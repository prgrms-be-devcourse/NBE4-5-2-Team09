export type TickerDto = {
  type: string; // 데이터 타입 (예: "ticker")
  code: string; // 마켓 코드 (예: "KRW-BTC")
  koreanName: string; // 한글 이름
  englishName: string; // 영어 이름
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
  RISE = 'RISE', // 상승
  EVEN = 'EVEN', // 보합
  FALL = 'FALL', // 하락
}

export enum MarketState {
  PREVIEW = 'PREVIEW', // 입금지원
  ACTIVE = 'ACTIVE', // 거래지원 가능
  DELISTED = 'DELISTED', // 거래지원 종료
}

export enum MarketWarning {
  NONE = 'NONE', // 유의 종목 아님
  CAUTION = 'CAUTION', // 유의 종목
}

export enum AskBid {
  ASK = 'ASK', // 매도
  BID = 'BID', // 매수
}

/** MockData 전용 타입 (추후 삭제 예정) **/
// OrderbookItem, Orderbook, TradeItem, CandleItem, NewsItem
export type TradeItem = {
  id: string;
  price: number;
  volume: number;
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

/** 실제 websocket 응답 객체 타입 **/
export type TradeDto = {
  type: 'trade'; // "trade"
  code: string; // 마켓 코드 (ex. KRW-BTC)
  tradePrice: number; // 체결 가격
  tradeVolume: number; // 체결량
  askBid: AskBid; // 매수/매도 구분
  prevClosingPrice: number; // 전일 종가
  change: Change; // 전일 대비
  changePrice: number; // 부호 없는 전일 대비 값
  tradeDate: string; // 체결 일자 (UTC 기준)
  tradeTime: string; // 체결 시각 (UTC 기준)
  tradeTimeStamp: number; // 체결 타임스탬프 (millisecond)
  timestamp: number; // 타임스탬프 (millisecond)
  sequentialId: number; // 체결 번호 (Unique)
  bestAskPrice: number; // 최우선 매도 호가
  bestAskSize: number; // 최우선 매도 잔량
  bestBidPrice: number; // 최우선 매수 호가
  bestBidSize: number; // 최우선 매수 잔량
  vwap: number; // 체결 기반 거래량 가중 평균가격 (VWAP)
  averageTradeSize: number; // 평균 체결 크기
  tradeImpact: number; // 체결가격 충격
};

export type OrderbookDto = {
  type: 'orderbook'; // "orderbook"
  code: string; // 마켓 코드 (ex. KRW-BTC)
  totalAskSize: number; // 호가 매도 총 잔량
  totalBidSize: number; // 호가 매수 총 잔량
  orderbookUnits: OrderbookUnit[]; // 상세 호가 정보 목록
  timestamp: number; // 타임스탬프 (millisecond)
  level: number; // 호가 모아보기 단위 (default:0)
  midPrice: number; // 중간 가격
  spread: number; // 매도/매수 호가 차이
  imbalance: number; // 잔량 불균형
  liquidityDepth: number; // 중간 가격 기준 ±X% 유동성 비율
};

export type OrderbookUnit = {
  askPrice: number; // 매도 호가
  bidPrice: number; // 매수 호가
  askSize: number; // 매도 잔량
  bidSize: number; // 매수 잔량
};

export type CandleChartDto = {
  market: string;
  candle_date_time_utc: string; // ISO8601 형식 문자열
  opening_price: number;
  high_price: number;
  low_price: number;
  trade_price: number;
  candle_acc_trade_volume: number;
  timestamp: number;
};

export type MarketDto = {
  code: string;
  koreanName: string;
  englishName: string;
};

export type MarketsDto = {
  content: MarketDto[];
} & PaginationDto;

export type PaginationDto = {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
