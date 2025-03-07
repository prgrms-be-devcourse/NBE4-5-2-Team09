"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import Cookies from "js-cookie";
import { parseJwt } from "../utils/parse-token"; // JWT 파싱 유틸

interface AuthContextType {
    accessToken: string | null;
    setAccessToken: (token: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    // 초기 상태: 쿠키에서 accessToken 읽어오기
    const [accessToken, setAccessTokenState] = useState<string | null>(() => {
        if (typeof window !== "undefined") {
            return Cookies.get("accessToken") || null;
        }
        return null;
    });

    // 쿠키와 상태를 동시에 업데이트하는 함수
    const setAccessToken = (token: string | null) => {
        setAccessTokenState(token);
        if (token) {
            Cookies.set("accessToken", token, {
                expires: 1,
                domain: window.location.hostname, // 현재 도메인에 저장
                secure: true,
                sameSite: "strict",
            });
        } else {
            Cookies.remove("accessToken", { domain: window.location.hostname });
        }
    };

    // 컴포넌트 마운트 시 액세스 토큰의 유효성을 체크하고 만료되었으면 리프레시 시도
    useEffect(() => {
        async function refreshAccessToken() {
            try {
                const response = await fetch("http://localhost:8080/api/auth/refresh", {
                    method: "POST",
                    credentials: "include",
                });
                if (response.ok) {
                    const authHeader = response.headers.get("Authorization");
                    if (authHeader?.startsWith("Bearer ")) {
                        const newToken = authHeader.slice(7);
                        setAccessToken(newToken);
                        console.log("새 액세스 토큰 발급됨:", newToken);
                    }
                } else {
                    console.error("리프레시 토큰 발급 실패:", response.status);
                }
            } catch (error) {
                console.error("리프레시 요청 중 오류 발생:", error);
            }
        }

        // 쿠키에 저장된 토큰이 존재하는지 확인
        const token = Cookies.get("accessToken");
        if (token) {
            const payload = parseJwt(token);
            const currentTime = Date.now() / 1000; // 초 단위
            if (payload && payload.exp && payload.exp < currentTime) {
                // 토큰 만료됨 → 리프레시 시도
                refreshAccessToken();
            }
        } else {
            // 토큰이 없으면 리프레시 엔드포인트에 요청 (리프레시 토큰이 있다면 새 토큰 발급 가능)
            refreshAccessToken();
        }
    }, []);

    return (
        <AuthContext.Provider value={{ accessToken, setAccessToken }}>
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
