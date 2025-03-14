'use client';

import { useState, useEffect } from 'react';
import type { NewsItem } from '@/types';

interface NewsListProps {
  news: NewsItem[];
}

export default function NewsList({ news: initialNews }: NewsListProps) {
  const [news, setNews] = useState<NewsItem[]>(initialNews);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // initialNews가 변경될 때 state를 업데이트
  useEffect(() => {
    setNews(initialNews);
    setCurrentPage(1);
  }, [initialNews]);

  // 새로고침 버튼 클릭 시 초기 데이터를 재설정하는 예시
  const refreshNews = async () => {
    setNews(initialNews);
    setCurrentPage(1);
  };

  // 페이지네이션 계산
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentNews = news.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(news.length / itemsPerPage);

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
        <div className="p-4 border-b border-muted flex justify-between items-center">
          <h2 className="text-lg font-semibold">뉴스</h2>
          <button
              onClick={refreshNews}
              className="px-3 py-1 text-sm border rounded hover:bg-muted"
          >
            새로고침
          </button>
        </div>

        <div className="overflow-y-auto max-h-[400px]">
          <ul className="divide-y divide-muted">
            {currentNews.map((item, index) => (
                <li key={item.id || index} className="p-4 hover:bg-muted">
                  <a
                      href={item.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="block"
                  >
                    <h3
                        className="font-medium mb-1"
                        dangerouslySetInnerHTML={{ __html: item.title }}
                    />
                    <p
                        className="text-sm text-secondary mb-2"
                        dangerouslySetInnerHTML={{ __html: item.summary }}
                    />
                    <div className="flex justify-between text-xs text-primary">
                      <span>{item.source}</span>
                      <span>{formatDate(item.publishedAt)}</span>
                    </div>
                  </a>
                </li>
            ))}
          </ul>
        </div>

        <div className="flex justify-center items-center p-4 space-x-4">
          <button
              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              className="px-3 py-1 text-sm border rounded disabled:opacity-50"
          >
            이전
          </button>
          <span className="text-sm">
            {currentPage} / {totalPages}
          </span>
          <button
              onClick={() =>
                  setCurrentPage((prev) => Math.min(prev + 1, totalPages))
              }
              disabled={currentPage === totalPages}
              className="px-3 py-1 text-sm border rounded disabled:opacity-50"
          >
            다음
          </button>
        </div>
      </div>
  );
}
