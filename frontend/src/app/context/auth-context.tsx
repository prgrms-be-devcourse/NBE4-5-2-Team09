"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import Cookies from "js-cookie";
import { parseJwt } from "../utils/parse-token"; // JWT 파싱 유틸

interface AuthContextType {
    accessToken: string | null;
    setAccessToken: (token: string | null) => void;
    isAuthLoading: boolean;
    customFetch: (input: RequestInfo, init?: RequestInit) => Promise<Response>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    // 초기 상태: 쿠키에서 accessToken 읽어오기
    const [accessToken, setAccessTokenState] = useState<string | null>(() => {
        if (typeof window !== "undefined") {
            const token = Cookies.get("accessToken") || null;
            console.log("[AuthProvider] 초기 accessToken:", token);
            return token;
        }
        return null;
    });
    const [isAuthLoading, setIsAuthLoading] = useState<boolean>(true);

    // 쿠키와 상태를 동시에 업데이트하는 함수
    const setAccessToken = (token: string | null) => {
        setAccessTokenState(token);
        if (token) {
            Cookies.set("accessToken", token, {
                expires: 1,
                domain: window.location.hostname, // 현재 도메인에 저장
                secure: false, // 개발 환경 테스트
                sameSite: "strict",
            });
            console.log("[AuthProvider] accessToken 저장:", token);
        } else {
            Cookies.remove("accessToken", { domain: window.location.hostname });
            console.log("[AuthProvider] accessToken 삭제");
        }
    };

    // 리프레시 토큰으로 새 액세스 토큰 발급 함수
    async function refreshAccessToken(): Promise<string | null> {
        console.log("[AuthProvider] refreshAccessToken 호출");
        try {
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL + "/api/auth/refresh"}`,
                {
                    method: "POST",
                    credentials: "include",
                }
            );
            console.log("[AuthProvider] refresh 응답 상태:", response.status);
            if (response.ok) {
                const authHeader = response.headers.get("Authorization");
                if (authHeader?.startsWith("Bearer ")) {
                    const newToken = authHeader.slice(7);
                    setAccessToken(newToken);
                    console.log("[AuthProvider] 새 액세스 토큰 발급됨:", newToken);
                    return newToken;
                } else {
                    console.warn("[AuthProvider] Authorization 헤더 없음");
                }
            } else {
                console.error("[AuthProvider] 리프레시 토큰 발급 실패:", response.status);
            }
        } catch (error) {
            console.error("[AuthProvider] 리프레시 요청 중 오류 발생:", error);
        }
        return null;
    }

    // 모든 API 요청에 대해 사용할 커스텀 fetch 함수
    const customFetch = async (
        input: RequestInfo,
        init?: RequestInit
    ): Promise<Response> => {
        console.log("[customFetch] 요청 시작:", input);
        // 현재 accessToken을 헤더에 추가
        const token = accessToken;
        let headers = {
            ...(init?.headers || {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        };

        console.log("[customFetch] 사용되는 헤더:", headers);
        let response = await fetch(input, {
            ...init,
            headers,
            credentials: "include",
        });
        console.log("[customFetch] 초기 응답 상태:", response.status);

        // 만약 403 응답이면, refresh token으로 새 액세스 토큰 발급 후 재요청
        if (response.status === 403) {
            console.warn(
                "[customFetch] 403 응답 감지됨. refresh token으로 재발급 시도합니다."
            );
            const newToken = await refreshAccessToken();
            if (newToken) {
                headers = {
                    ...init?.headers,
                    Authorization: `Bearer ${newToken}`,
                };
                console.log("[customFetch] 새 토큰으로 재요청 헤더:", headers);
                response = await fetch(input, {
                    ...init,
                    headers,
                    credentials: "include",
                });
                console.log("[customFetch] 재요청 응답 상태:", response.status);
            } else {
                console.error(
                    "[customFetch] refresh token 재발급 실패. 로그인 페이지로 리다이렉트합니다."
                );
                window.location.href = "/login";
            }
        }

        return response;
    };

    // 컴포넌트 마운트 시 액세스 토큰의 유효성을 체크하고, 로그인 상태일 때만 refresh 시도
    useEffect(() => {
        async function validateToken() {
            console.log("[AuthProvider] validateToken 시작");
            const token = Cookies.get("accessToken");
            if (token) {
                const payload = parseJwt(token);
                const currentTime = Date.now() / 1000; // 초 단위
                console.log(
                    "[AuthProvider] 토큰 만료 시간:",
                    payload?.exp,
                    "현재 시간:",
                    currentTime
                );
                if (payload && payload.exp && payload.exp < currentTime) {
                    console.warn("[AuthProvider] 토큰 만료됨 → refreshAccessToken 호출");
                    await refreshAccessToken();
                }
            } else {
                console.warn("[AuthProvider] 쿠키에 토큰 없음 → 로그인 상태 아님");
                // 로그인 상태가 아니라면 refresh를 시도하지 않습니다.
            }
            setIsAuthLoading(false);
            console.log("[AuthProvider] validateToken 완료, isAuthLoading false");
        }
        validateToken();
    }, []);

    return (
        <AuthContext.Provider
            value={{ accessToken, setAccessToken, isAuthLoading, customFetch }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth는 AuthProvider 내에서 사용해야 합니다.");
    }
    return context;
}
