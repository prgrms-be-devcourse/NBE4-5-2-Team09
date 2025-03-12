'use client';

import { useAuth } from '@/context/AuthContext';
import NavLink from '@/components/NavLink';
import {usePathname, useRouter} from 'next/navigation';
import { useState, useEffect } from 'react';
import Link from 'next/link';

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { accessToken, isAuthLoading, setAccessToken, customFetch } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [userName, setUserName] = useState(''); // 백엔드에서 받아올 사용자 이름
  const isLoggedIn = !!accessToken;

  // accessToken이 변경되면 백엔드에 사용자 정보를 요청
  useEffect(() => {
    const fetchUserInfo = async () => {
      if (!accessToken) return;
      try {
        const res = await customFetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/info', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
        });
        if (res.ok) {
          const data = await res.json();
          setUserName(data.name || '');
        } else {
          console.error('사용자 정보를 가져오지 못했습니다.');
          setUserName('');
        }
      } catch (error) {
        console.error('사용자 정보 요청 실패:', error);
        setUserName('');
      }
    };

    fetchUserInfo();
  }, [accessToken, customFetch]);

  const handleLogout = async () => {
    if (!accessToken) return;
    setIsLoggingOut(true);
    try {
      const res = await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/logout', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
      });
      if (res.ok) {
        setAccessToken(null);
        router.push('/user/login');
      } else {
        const errorData = await res.json();
        alert(`로그아웃 실패: ${errorData.message || ''}`);
      }
    } catch (err) {
      console.error('로그아웃 오류:', err);
      alert('로그아웃 중 오류가 발생했습니다.');
    } finally {
      setIsLoggingOut(false);
    }
  };

  if (isAuthLoading) {
    return (
      <header className="bg-white border-b border-gray-200">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-8">
            <nav className="hidden md:flex space-x-8">
              <NavLink href="/">코인 대시보드</NavLink>
              <NavLink href="/bookmark">북마크 대시보드</NavLink>
              <NavLink href="/etc">기타 메뉴</NavLink>
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
              <NavLink href="/">코인 대시보드</NavLink>
              <NavLink href="/bookmark">북마크 대시보드</NavLink>
              <NavLink href="/etc">기타 메뉴</NavLink>
            </nav>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-500">최근 업데이트: 5초 전</span>
            <div className="flex items-center">
              {/* 회원 이름 클릭 시 내 정보 페이지로 이동 */}
              <Link href={isLoggedIn ? '/user/info' : '#'}>
                <span
                  className={`ml-2 text-sm font-medium ${
                    isLoggedIn
                      ? 'text-blue-500 hover:text-blue-700 underline cursor-pointer'
                      : 'text-gray-400 cursor-default'
                  }`}
                >
                  {isLoggedIn ? userName : '게스트'}
                </span>
              </Link>
            </div>
            {/* 로그인 페이지일 때는 로그인 버튼 숨김 */}
            {pathname !== '/user/login' && (
                isLoggedIn ? (
                    <button
                        onClick={handleLogout}
                        disabled={isLoggingOut}
                        className="bg-blue-500 hover:bg-blue-600 text-white cursor-pointer px-4 py-2 rounded-md text-sm"
                    >
                      {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
                    </button>
                ) : (
                    <button
                        onClick={() => router.push('/user/login')}
                        className="bg-blue-500 hover:bg-blue-600 text-white cursor-pointer px-4 py-2 rounded-md text-sm"
                    >
                      로그인
                    </button>
                )
            )}
          </div>
        </div>
      </div>
    </header>
  );
}