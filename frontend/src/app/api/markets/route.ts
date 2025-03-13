import { client } from '@/lib/api';
import { MarketsDto } from '@/types';
import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
): Promise<NextResponse<MarketsDto | { error: unknown }>> {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get('type') || 'KRW';
  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '10', 10);

  const response = await client.GET('/api/market', {
    params: {
      query: {
        type,
        page,
        size,
      },
    },
  });

  if (response.error) {
    return NextResponse.json({ error: response['error'] }, { status: 400 });
  }

  const markets: MarketsDto = response.data as MarketsDto;
  return NextResponse.json(markets);
}
