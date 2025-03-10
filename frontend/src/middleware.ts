import { NextRequest, NextResponse } from "next/server";
import {
  verifyAccessToken,
  refreshTokens,
  setMultipleCookies,
} from "@/lib/helpers";

// 보호할 경로 목록
const protectedRoutes = ["/dashboard"];

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const isProtected = protectedRoutes.some((route) =>
    pathname.startsWith(route)
  );

  // 보호되지 않은 경로는 그냥 통과
  if (!isProtected) {
    return NextResponse.next();
  }

  // 쿠키에서 accessToken 읽기
  const accessToken = request.cookies.get("accessToken")?.value ?? "";
  const isValid = await isAccessTokenValid(accessToken);

  // 유효한 액세스 토큰이 있으면 그대로 통과하고, Authorization 헤더에 설정
  if (isValid) {
    const response = NextResponse.next();
    if (accessToken) {
      response.headers.set("Authorization", `Bearer ${accessToken}`);
    }
    return response;
  }

  // 액세스 토큰이 유효하지 않으면 refreshTokens()로 리프레시 시도
  const refreshResponse = await refreshTokens(request);
  if (
    !refreshResponse.ok ||
    !refreshResponse.data ||
    !refreshResponse.data.newAccessToken
  ) {
    // 리프레시 실패 시 로그인 페이지로 리다이렉트
    return NextResponse.redirect(new URL("/user/login", request.url));
  }

  const { newAccessToken, setCookieHeaders } = refreshResponse.data;

  const res = NextResponse.next();
  // 백엔드에서 전달한 추가 쿠키 설정 헤더들을 적용
  setMultipleCookies(res, setCookieHeaders);

  // 새 액세스 토큰을 쿠키에 저장
  res.cookies.set("accessToken", newAccessToken, {
    httpOnly: true,
    secure: true,
    sameSite: "strict",
    path: "/",
  });
  // 응답 헤더에 새 액세스 토큰 설정
  res.headers.set("Authorization", `Bearer ${newAccessToken}`);

  return res;
}

export const config = {
  matcher: ["/((?!_next|.*\\..*).*)"],
};

async function isAccessTokenValid(accessToken: string): Promise<boolean> {
  if (!accessToken) return false;
  try {
    await verifyAccessToken(accessToken);
    return true;
  } catch (err) {
    return false;
  }
}
