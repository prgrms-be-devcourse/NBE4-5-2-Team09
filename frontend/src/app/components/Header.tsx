"use client";

import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "../context/auth-context";
import { useState, useMemo } from "react";
import { parseJwt } from "../utils/parse-token";

export default function Header() {
  const router = useRouter();
  const { accessToken, isAuthLoading, setAccessToken } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  // JWT 토큰에서 사용자 이름 추출 (토큰이 없으면 빈 문자열)
  const tokenPayload = useMemo(() => parseJwt(accessToken), [accessToken]);
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

  // 로딩 상태일 때: 간단한 플레이스홀더 표시
  if (isAuthLoading) {
    return (
        <header className="bg-white border-b border-gray-200">
          <div className="container mx-auto px-4 py-4 flex justify-between items-center">
            <div className="flex items-center space-x-8">
              <nav className="hidden md:flex space-x-8">
                <Link
                    href="/"
                    className="text-gray-900 font-medium border-b-2 border-blue-500 pb-1"
                >
                  코인 대시보드
                </Link>
                <Link
                    href="/market"
                    className="text-gray-500 hover:text-gray-900"
                >
                  북마크 대시보드
                </Link>
                <Link href="/etc" className="text-gray-500 hover:text-gray-900">
                  기타 메뉴
                </Link>
              </nav>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500">최근 업데이트: 5초 전</span>
              <div className="flex items-center">
                <span className="ml-2 text-sm font-medium">로딩중...</span>
              </div>
              <button
                  className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm"
                  disabled
              >
                로그아웃
              </button>
            </div>
          </div>
        </header>
    );
  }

  return (
      <header className="bg-white border-b border-gray-200">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center space-x-8">
              <nav className="hidden md:flex space-x-8">
                <Link
                    href="/"
                    className="text-gray-900 font-medium border-b-2 border-blue-500 pb-1"
                >
                  코인 대시보드
                </Link>
                <Link
                    href="/market"
                    className="text-gray-500 hover:text-gray-900"
                >
                  북마크 대시보드
                </Link>
                <Link href="/etc" className="text-gray-500 hover:text-gray-900">
                  기타 메뉴
                </Link>
              </nav>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500">최근 업데이트: 5초 전</span>
              <div className="flex items-center">
              <span className="ml-2 text-sm font-medium">
                {isLoggedIn ? userName : "게스트"}
              </span>
              </div>
              {isLoggedIn ? (
                  <button
                      onClick={handleLogout}
                      disabled={isLoggingOut}
                      className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm"
                  >
                    {isLoggingOut ? "로그아웃 중..." : "로그아웃"}
                  </button>
              ) : (
                  <button
                      onClick={() => router.push("/user/login")}
                      className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm"
                  >
                    로그인
                  </button>
              )}
            </div>
          </div>
        </div>
      </header>
  );
}
