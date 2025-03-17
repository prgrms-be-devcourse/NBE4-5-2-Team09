import { NextResponse } from 'next/server';

export type NewsItem = {
  id: string;
  title: string;
  summary: string;
  source: string;
  publishedAt: string;
  url: string;
};

// API 응답의 원시 뉴스 항목 타입을 정의합니다.
interface RawNewsItem {
  id?: string;
  link?: string;
  title: string;
  summary?: string;
  description?: string;
  source?: string;
  originallink?: string;
  publishedAt?: string;
  pubDate?: string;
  url?: string;
}

// 전체 API 응답의 타입을 정의합니다.
interface NewsApiResponse {
  items: RawNewsItem[];
}

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const market = searchParams.get('market');
  if (!market) {
    return NextResponse.json({ error: 'market parameter is required' }, { status: 400 });
  }

  try {
    const url = `${process.env.NEXT_PUBLIC_API_URL}/api/news?market=${encodeURIComponent(
      market,
    )}&display=100&start=1&sort=sim&format=json`;
    const response = await fetch(url);
    if (!response.ok) {
      return NextResponse.json({ error: 'Failed to fetch news' }, { status: response.status });
    }
    // API 응답의 타입을 명시적으로 지정합니다.
    const data = (await response.json()) as NewsApiResponse;
    if (data && Array.isArray(data.items)) {
      const transformedNews: NewsItem[] = data.items.map((item: RawNewsItem) => ({
        id: item.id || item.link || '', // id가 없으면 link를 id로 사용, 없으면 빈 문자열 반환
        title: item.title,
        summary: item.summary || item.description || '',
        source: item.source || (item.originallink ? new URL(item.originallink).hostname : ''),
        publishedAt: item.publishedAt || item.pubDate || '',
        url: item.url || item.link || '',
      }));
      return NextResponse.json(transformedNews, { status: 200 });
    } else {
      return NextResponse.json({ error: 'Unexpected data format' }, { status: 500 });
    }
  } catch (err) {
    console.error('뉴스 데이터 호출 오류:', err);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
