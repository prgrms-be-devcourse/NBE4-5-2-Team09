'use client';

import type { NewsItem } from '@/types';

interface NewsListProps {
  news: NewsItem[];
}

export default function NewsList({ news }: NewsListProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="bg-card rounded-lg shadow-sm overflow-hidden">
      <div className="p-4 border-b border-muted">
        <h2 className="text-lg font-semibold">뉴스</h2>
      </div>

      <div className="overflow-y-auto max-h-[400px]">
        <ul className="divide-y divide-muted">
          {news.map((item) => (
            <li key={item.id} className="p-4 hover:bg-muted">
              <a href={item.url} className="block">
                <h3 className="font-medium mb-1">{item.title}</h3>
                <p className="text-sm text-secondary mb-2">{item.summary}</p>
                <div className="flex justify-between text-xs text-primary">
                  <span>{item.source}</span>
                  <span>{formatDate(item.publishedAt)}</span>
                </div>
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
