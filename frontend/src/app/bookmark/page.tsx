'use client';

import { useEffect, useState } from 'react';
import ClientPage from '@/app/bookmark/ClientPage';
import WebSocketProvider from '@/context/WebSocketContext';
import { components } from '@/lib/api/generated/schema';
import { useAuth } from '@/context/AuthContext';
import { client } from '@/lib/api';
import RequireAuthenticated from '@/components/RequireAutenticated';

type BookmarkResponse = components['schemas']['BookmarkResponse'];
type PageBookmarkResponse = components['schemas']['PagedResponseBookmarkResponse'];

export default function Page() {
  const { accessToken } = useAuth();
  const [bookmarksData, setBookmarksData] = useState<PageBookmarkResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const itemsPerPage = 9;
  const quote = 'KRW';

  useEffect(() => {
    async function fetchBookmarks() {
      try {
        const { data, error: fetchError } = await client.GET('/api/bookmarks/{quote}', {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
          params: {
            path: { quote },
            query: {
              page: 0,
              size: itemsPerPage,
            },
          },
        });

        if (fetchError || !data?.content) {
          setError('북마크 데이터를 불러오는 중 오류가 발생했습니다.');
          return;
        }

        // 전체 북마크 페이지 데이터
        const pageData: PageBookmarkResponse = data;

        // KRW 마켓 필터링
        const filteredPageData: PageBookmarkResponse = {
          ...pageData,
          content:
            pageData.content?.filter((bookmark): bookmark is Required<BookmarkResponse> =>
              Boolean(
                bookmark.id &&
                  bookmark.code?.startsWith('KRW-') &&
                  bookmark.koreanName &&
                  bookmark.englishName &&
                  bookmark.createAt,
              ),
            ) ?? [],
        };

        setBookmarksData(filteredPageData);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : '북마크 데이터를 불러오는 중 오류가 발생했습니다.',
        );
      }
    }
    fetchBookmarks();
  }, [accessToken]);

  if (error) {
    return renderError(error);
  }
  if (!bookmarksData) {
    return <div className="p-6 flex justify-center items-center">로딩 중...</div>;
  }

  // WebSocketProvider를 감싸서 ClientPage에 데이터를 전달합니다.
  return (
    <WebSocketProvider subscriptions={[]}>
      <ClientPage bookmarks={bookmarksData} />
    </WebSocketProvider>
  );
}

// 에러 메시지 렌더링 함수
function renderError(message: string) {
  return (
    <RequireAuthenticated>
      <div className="p-6 flex justify-center items-center">
        <p className="text-red-500">{message}</p>
      </div>
    </RequireAuthenticated>
  );
}
