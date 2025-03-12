'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';

export default function UserInfoPage() {
  const { accessToken } = useAuth();
  const router = useRouter();
  const [userInfo, setUserInfo] = useState<{ name: string; email: string } | null>(null);
  const [error, setError] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  // 사용자 정보 fetch
  useEffect(() => {
    if (!accessToken) {
      setError('로그인이 필요합니다.');
      return;
    }
    async function fetchUserInfo() {
      try {
        const res = await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/info', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${accessToken}`,
          },
          credentials: 'include',
        });
        if (!res.ok) {
          throw new Error('사용자 정보를 불러오지 못했습니다.');
        }
        const data = await res.json();
        setUserInfo(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '오류 발생');
      }
    }
    fetchUserInfo();
  }, [accessToken]);

  // 회원 탈퇴 핸들러
  const handleSignOut = async () => {
    if (!confirm('정말 회원 탈퇴 하시겠습니까?')) return;
    if (!password) {
      alert('비밀번호를 입력하세요.');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/signout', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
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

  if (error) return <div className="p-6">{error}</div>;
  if (!userInfo) return <div className="p-6">로딩 중...</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">내 정보</h1>
      <p>
        <strong>이름:</strong> {userInfo.name}
      </p>
      <p>
        <strong>이메일:</strong> {userInfo.email}
      </p>
      <div className="mt-4">
        <label htmlFor="password" className="block font-medium">
          회원 탈퇴를 위해 비밀번호 입력:
        </label>
        <input
          type="password"
          id="password"
          className="border p-2 mt-1 w-64"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>
      <div className="mt-4">
        <Button onClick={handleSignOut} disabled={loading} className="cursor-pointer">
          {loading ? '처리 중...' : '회원 탈퇴'}
        </Button>
      </div>
    </div>
  );
}
