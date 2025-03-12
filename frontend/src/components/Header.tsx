'use client';

import { useAuth } from '@/context/AuthContext';
import NavLink from '@/components/NavLink';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import Link from 'next/link';
import { Coins, Bookmark, CircleUser } from 'lucide-react';
import { Button } from './ui/button';
import { useUserStore } from '@/store/user.store';

const navItems = [
  { name: '코인 대시보드', href: '/coin/list', icon: Coins },
  { name: '북마크 대시보드', href: '/bookmark', icon: Bookmark },
];

export default function Header() {
  const router = useRouter();
  const { accessToken, isAuthLoading, setAccessToken } = useAuth();
  const { user, deleteUser } = useUserStore();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const isLoggedIn = !!accessToken;

  const handleLogout = async () => {
    if (!accessToken) return;
    setIsLoggingOut(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/logout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
      });
      if (res.ok) {
        setAccessToken(null);
        deleteUser();
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

  return (
    <header className="bg-white border-b border-gray-200">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <div className="flex items-center space-x-8">
          <Link href="/">
            <div className="flex">
              <img src="/logo.svg" alt="Coing Logo" className="h-8 mr-2" />
              <span className="text-xl font-bold text-secondary">Coing</span>
            </div>
          </Link>
          <nav className="hidden md:flex space-x-8">
            {navItems.map((item) => (
              <NavLink key={item.href} href={item.href}>
                <item.icon className="w-4" />
                {item.name}
              </NavLink>
            ))}
          </nav>
        </div>

        <div className="flex items-center space-x-4">
          <Link
            href="/user/info"
            className="flex items-center gap-1 text-sm font-medium text-secondary hover:text-card-foreground cursor-pointer"
          >
            <CircleUser />
            {user?.name || 'Guest'}
          </Link>
          {isAuthLoading ? (
            <Button disabled>로딩중...</Button>
          ) : isLoggedIn ? (
            <Button className="cursor-pointer" onClick={handleLogout} disabled={isLoggingOut}>
              {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
            </Button>
          ) : (
            <Button className="cursor-pointer" onClick={() => router.push('/user/login')}>
              로그인
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
