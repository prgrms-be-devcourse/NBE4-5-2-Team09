import { NextRequest, NextResponse } from "next/server";
import {
  verifyAccessToken,
  refreshTokens,
  setMultipleCookies,
} from "@/lib/auth-helpers";

const protectedRoutes = ["/user"];

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const isProtected = protectedRoutes.some((route) =>
    pathname.startsWith(route)
  );

  if (!isProtected) {
    return NextResponse.next();
  }

  // AccessToken 검사
  const accessToken = request.cookies.get("accessToken")?.value ?? "";
  const isValid = await isAccessTokenValid(accessToken);

  // AccessToken이 유효하면 => Authorization 헤더 추가 후 통과
  if (isValid) {
    const response = NextResponse.next();
    response.headers.set("Authorization", `Bearer ${accessToken}`);
    return response;
  }

  // refresh
  const refreshResponse = await refreshTokens(request);
  if (
    !refreshResponse.ok ||
    !refreshResponse.data ||
    !refreshResponse.data.newAccessToken
  ) {
    return NextResponse.redirect(new URL("/user/login", request.url));
  }

  const { newAccessToken, setCookieHeaders } = refreshResponse.data;
  const res = NextResponse.next();

  setMultipleCookies(res, setCookieHeaders);

  res.cookies.set("accessToken", newAccessToken, {
    httpOnly: true,
    secure: true,
    sameSite: "strict",
    path: "/",
  });
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
