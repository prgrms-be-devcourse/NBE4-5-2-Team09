import { cookies } from "next/headers";
import ClientPage from "@/app/bookmark/ClientPage";
import WebSocketProvider from "@/context/WebSocketContext";
import client from "@/lib/api/client";
import { components } from "@/lib/api/generated/schema";

type BookmarkResponse = components["schemas"]["BookmarkResponse"];
type PageBookmarkResponse =
  components["schemas"]["PagedResponseBookmarkResponse"];

export default async function Page() {
  // ✅ accessToken을 ClientPage.tsx로 넘기지 않음
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("accessToken")?.value;
  const itemsPerPage = 9;
  const quote = "KRW";

  if (!accessToken) {
    return renderError("로그인이 만료되었습니다.");
  }

  try {
    const { data, error } = await client.GET("/api/bookmarks/{quote}", {
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

    if (error || !data?.content) {
      return renderError("북마크 데이터를 불러오는 중 오류가 발생했습니다.");
    }

    // ✅ 전체 북마크 페이지 데이터
    const pageData: PageBookmarkResponse = data;

    // ✅ KRW 마켓만 필터링하여 `content` 필드 수정
    const filteredPageData: PageBookmarkResponse = {
      ...pageData,
      content:
        pageData.content?.filter(
          (bookmark): bookmark is Required<BookmarkResponse> =>
            Boolean(
              bookmark.id &&
                bookmark.code?.startsWith("KRW-") && // ✅ KRW 마켓 필터링
                bookmark.koreanName &&
                bookmark.englishName &&
                bookmark.createAt
            )
        ) ?? [],
    };

    // ✅ WebSocket 구독용 KRW 마켓 리스트 생성
    const markets = filteredPageData.content!!.map(
      (bookmark) => bookmark.code!
    );

    return (
      <WebSocketProvider subscriptions={[{ type: "ticker", markets }]}>
        <ClientPage bookmarks={filteredPageData} markets={markets} />
      </WebSocketProvider>
    );
  } catch (error) {
    console.error("Error fetching bookmarks:", error);
    return renderError(
      error instanceof Error
        ? error.message
        : "북마크 데이터를 불러오는 중 오류가 발생했습니다."
    );
  }
}

// ✅ 에러 메시지를 렌더링하는 함수
function renderError(message: string) {
  return (
    <div className="p-6 flex justify-center items-center">
      <p className="text-red-500">{message}</p>
    </div>
  );
}
