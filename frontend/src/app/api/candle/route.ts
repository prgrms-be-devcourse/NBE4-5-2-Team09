import { NextRequest, NextResponse } from 'next/server';
import { client } from '@/lib/api/client';
import { CandleChartDto, CandleItem } from '@/types';

export async function GET(request: NextRequest): Promise<NextResponse> {
  const { searchParams } = new URL(request.url);
  const minuteUnit = searchParams.get('unit') || '';
  const market = searchParams.get('market') || 'KRW-BTC';
  const type = searchParams.get('candleType') || 'seconds';

  const query: Record<string, string> = {};
  if (type === 'minutes' && minuteUnit) {
    query.unit = minuteUnit;
  }

  try {
    const response = await client.GET('/api/candles/{market}/{type}', {
      params: {
        path: {
          market,
          type,
        },
        query,
      },
    });

    if (response.error) {
      return NextResponse.json({ error: response['error'] }, { status: 400 });
    }

    const data = response.data as CandleChartDto[];
    const mapped: CandleItem[] = data.map((dto) => ({
      time: new Date(dto.candle_date_time_utc).getTime(),
      open: dto.opening_price,
      high: dto.high_price,
      low: dto.low_price,
      close: dto.trade_price,
      volume: dto.candle_acc_trade_volume,
    }));

    return NextResponse.json(mapped);
  } catch (error: any) {
    console.error('캔들 데이터 호출 오류:', error);
    return NextResponse.json({ error: error.message || '캔들 데이터 호출 오류' }, { status: 500 });
  }
}
