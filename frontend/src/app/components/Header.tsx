"use client";

import { useRouter } from "next/navigation";
import { useAuth } from "../context/auth-context";
import { useState, useEffect, useMemo } from "react";
import { parseJwt } from "../utils/parse-token"; // JWT 파싱 유틸 (한글 깨짐 방지)

export default function Header() {
  const router = useRouter();
  const { accessToken, isAuthLoading, setAccessToken } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  // JWT 토큰에서 사용자 이름 추출 (토큰이 없으면 빈 문자열)
  const tokenPayload = useMemo(() => parseJwt(accessToken), [accessToken]);
  // 백엔드에서 반환한 토큰의 name 필드는 이미 일반 문자열이어야 함
  const userName = tokenPayload?.name ?? "";

  const isLoggedIn = !!accessToken;

  const handleLogout = async () => {
    if (!accessToken) return;
    setIsLoggingOut(true);
    try {
      const res = await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          // 로그인 시 헤더로 전달된 액세스 토큰 사용
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: "include",
      });
      if (res.ok) {
        setAccessToken(null);
        router.push("/user/login");
      } else {
        const errorData = await res.json();
        alert(`로그아웃 실패: ${errorData.message || ""}`);
      }
    } catch (err) {
      console.error("로그아웃 오류:", err);
      alert("로그아웃 중 오류가 발생했습니다.");
    } finally {
      setIsLoggingOut(false);
    }
  };

  // 로딩 상태일 때: 새로고침 후 토큰 재발급(리프레시)이 진행 중이면 일정한 헤더 레이아웃을 유지
  if (isAuthLoading) {
    return (
        <header className="bg-white border-b border-gray-200">
          <div className="container mx-auto px-4 py-4 flex justify-between items-center">
            <div className="text-gray-700 font-medium">COING</div>
            <div className="flex items-center space-x-4">
              {/* 로딩 플레이스홀더 (버튼과 같은 크기) */}
              <div className="w-32 h-10 bg-gray-300 rounded animate-pulse"></div>
            </div>
          </div>
        </header>
    );
  }

  return (
      <header className="bg-white border-b border-gray-200">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <div className="text-gray-700 font-medium">COING</div>
          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
                <>
                  <span className="text-gray-700 font-semibold">{userName}</span>
                  <button
                      onClick={handleLogout}
                      disabled={isLoggingOut}
                      className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
                  >
                    {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
                  </button>
                </>
            ) : (
                <button
                    onClick={() => router.push("/user/login")}
                    className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
                >
                  로그인
                </button>
            )}
          </div>
        </div>
      </header>
  );
}
