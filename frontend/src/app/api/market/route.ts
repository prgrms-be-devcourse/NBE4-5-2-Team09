import { client } from '@/lib/api';
import { MarketDto } from '@/types';
import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
): Promise<NextResponse<MarketDto | { error: unknown }>> {
  const { searchParams } = new URL(request.url);
  const code = searchParams.get('code') || 'KRW-BTC';
  const response = await client.GET('/api/market/{code}', {
    params: {
      path: {
        code,
      },
    },
  });

  if (response.error) {
    return NextResponse.json({ error: response['error'] }, { status: 400 });
  }
  const market: MarketDto = response.data as MarketDto;
  return NextResponse.json(market);
}
