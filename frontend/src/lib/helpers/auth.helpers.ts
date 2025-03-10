import { jwtVerify } from "jose";
import { NextRequest, NextResponse } from "next/server";
import client from "../api/client";

export async function verifyAccessToken(accessToken: string) {
  if (!accessToken) {
    throw new Error("No AccessToken");
  }
  const secret = process.env.NEXT_SESSION_SECRET ?? "secret_key";
  const encodedKey = new TextEncoder().encode(secret);

  const { payload } = await jwtVerify(accessToken, encodedKey, {
    algorithms: ["HS256"],
  });
  return payload;
}

export async function refreshTokens(request: NextRequest) {
  try {
    const response = await client.POST("/api/auth/refresh", {
      headers: {
        cookie: request.headers.get("cookie") ?? "",
      },
    });

    if (response.error) {
      return { ok: false, data: null };
    }
    const data = response.data;
    const setCookieHeaders = response.response.headers.getSetCookie();

    let newAccessToken = null;
    try {
      const parsedData = JSON.parse(data.detail!);
      newAccessToken = parsedData.accessToken;
    } catch (error) {
      console.error("JSON 파싱 오류:", error);
    }

    return {
      ok: true,
      data: {
        newAccessToken,
        setCookieHeaders,
      },
    };
  } catch (err) {
    console.error("refreshTokens error:", err);
    return { ok: false, data: null };
  }
}

export function setMultipleCookies(res: NextResponse, cookieHeaders: string[]) {
  if (!cookieHeaders || cookieHeaders.length === 0) return;
  for (const cookie of cookieHeaders) {
    res.headers.append("set-cookie", cookie);
  }
}
