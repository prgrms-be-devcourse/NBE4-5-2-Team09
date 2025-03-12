'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import RequireAuthenticated from '@/components/RequireAutenticated';

export default function UserInfoPage() {
  const { accessToken, customFetch } = useAuth();
  const router = useRouter();
  const [userInfo, setUserInfo] = useState<{ name: string; email: string } | null>(null);
  const [error, setError] = useState<string>('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  // 사용자 정보 fetch
  useEffect(() => {
    if (!accessToken) {
      setFetching(false);
      return;
    }
    const fetchUserInfo = async () => {
      try {
        const res = await customFetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/info', {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
        });
        if (!res.ok) {
          throw new Error('사용자 정보를 불러오지 못했습니다.');
        }
        const data = await res.json();
        setUserInfo(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '오류 발생');
      } finally {
        setFetching(false);
      }
    };
    fetchUserInfo();
  }, [accessToken, customFetch]);

  // 회원 탈퇴 핸들러
  const handleSignOut = async () => {
    if (!confirm('정말 회원 탈퇴 하시겠습니까?')) return;
    if (!password) {
      alert('비밀번호를 입력하세요.');
      return;
    }
    setLoading(true);
    try {
      const res = await customFetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/signout', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ password }),
      });
      if (!res.ok) {
        const errorData = await res.json();
        alert('회원 탈퇴 실패: ' + (errorData.message || ''));
      } else {
        alert('회원 탈퇴 성공');
        router.push('/user/login');
      }
    } catch (err) {
      console.error(err);
      alert('회원 탈퇴 중 오류 발생');
    } finally {
      setLoading(false);
    }
  };

  if (error) {
    return (
      <RequireAuthenticated>
        <div className="p-6 text-center text-red-500">{error}</div>
      </RequireAuthenticated>
    );
  }

  if (fetching || !userInfo) {
    return (
      <RequireAuthenticated>
        <div className="p-6 text-center">로딩 중...</div>
      </RequireAuthenticated>
    );
  }

  return (
    <RequireAuthenticated>
      <div className="p-6 max-w-md mx-auto">
        <h1 className="text-2xl font-bold mb-4">내 정보</h1>
        <p className="mb-2">
          <strong>이름:</strong> {userInfo.name}
        </p>
        <p className="mb-4">
          <strong>이메일:</strong> {userInfo.email}
        </p>
        <div className="mt-4">
          <label htmlFor="password" className="block font-medium mb-1">
            회원 탈퇴를 위해 비밀번호 입력:
          </label>
          <input
            type="password"
            id="password"
            className="border p-2 w-full"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력하세요"
          />
        </div>
        <div className="mt-4">
          <Button onClick={handleSignOut} disabled={loading}>
            {loading ? '처리 중...' : '회원 탈퇴'}
          </Button>
        </div>
      </div>
    </RequireAuthenticated>
  );
}
