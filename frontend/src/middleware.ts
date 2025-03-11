import { NextRequest, NextResponse } from "next/server";
import { refreshTokens, setMultipleCookies } from "@/lib/helpers";

// 보호할 경로 목록
const protectedRoutes = ["/dashboard"];

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const isProtected = protectedRoutes.some((route) => pathname.startsWith(route));

  // 보호되지 않은 경로는 그대로 통과
  if (!isProtected) {
    return NextResponse.next();
  }

  // 클라이언트에서는 액세스 토큰을 sessionStorage에 보관하므로, 미들웨어는 쿠키에 저장된 리프레시 토큰만 접근할 수 있습니다.
  const refreshToken = request.cookies.get("refreshToken")?.value;
  if (!refreshToken) {
    // 리프레시 토큰이 없다면 로그인 페이지로 리다이렉트
    return NextResponse.redirect(new URL("/user/login", request.url));
  }

  // 리프레시 토큰을 사용해 새 액세스 토큰을 발급받음
  const refreshResponse = await refreshTokens(request);
  if (
      !refreshResponse.ok ||
      !refreshResponse.data ||
      !refreshResponse.data.newAccessToken
  ) {
    // 새 토큰 발급에 실패하면 로그인 페이지로 리다이렉트
    return NextResponse.redirect(new URL("/user/login", request.url));
  }

  const { newAccessToken, setCookieHeaders } = refreshResponse.data;

  // NextResponse를 생성하여 추가 쿠키 설정과 응답 헤더에 새 액세스 토큰을 담음
  const response = NextResponse.next();

  // 서버 측에서 추가로 갱신된 리프레시 토큰 등을 쿠키로 설정할 필요가 있으면 setMultipleCookies 함수를 사용
  setMultipleCookies(response, setCookieHeaders);

  // 액세스 토큰은 쿠키에 저장하지 않고, 응답 헤더의 Authorization에 포함시켜 클라이언트가 이를 추출해 sessionStorage에 업데이트하도록 함
  response.headers.set("Authorization", `Bearer ${newAccessToken}`);

  return response;
}

export const config = {
  matcher: ["/((?!_next|.*\\..*).*)"],
};
