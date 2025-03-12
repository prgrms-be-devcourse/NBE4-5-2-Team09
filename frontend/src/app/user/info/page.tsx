'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { useUserStore } from '@/store/user.store';
import { toast } from 'sonner';
import RequireAuthenticated from '@/components/RequireAutenticated';
import { Input } from '@/components/ui/input';
import { Mail, Lock } from 'lucide-react';

export default function UserInfoPage() {
  const { accessToken, customFetch } = useAuth();
  const { user, deleteUser } = useUserStore();
  const router = useRouter();

  const [error, setError] = useState<string>('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  // user 상태가 업데이트되면 fetching 상태를 false로 설정
  useEffect(() => {
    if (user) {
      setFetching(false);
    }
  }, [user]);

  // 회원 탈퇴 핸들러
  const handleSignOut = async () => {
    if (!confirm('정말 회원 탈퇴 하시겠습니까?')) return;
    if (!password) {
      toast.error('비밀번호를 입력하세요.');
      return;
    }
    setLoading(true);
    try {
      // 내부 API Route를 호출 (relative path 사용)
      const res = await customFetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/signout', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ password }),
      });
      if (!res.ok) {
        const errorData = await res.json();
        toast.error(`회원 탈퇴 실패: ${errorData.message || ''}`);
      } else {
        toast.success('회원 탈퇴 성공');
        deleteUser();
        router.push('/user/login');
      }
    } catch (err) {
      console.error('회원 탈퇴 오류:', err);
      toast.error('회원 탈퇴 중 오류 발생');
    } finally {
      setLoading(false);
    }
  };

  if (error) {
    return <div className="w-full flex justify-center text-background text-red-500">{error}</div>;
  }

  if (fetching || !user) {
    return <div className="w-full flex justify-center text-background">로딩 중...</div>;
  }

  return (
    <RequireAuthenticated>
      <div className="mt-10 max-w-md mx-auto bg-card shadow-lg rounded-lg p-8">
        <h1 className="text-3xl font-bold text-center mb-6">내 정보</h1>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-muted-foreground">이름</label>
            <p className="mt-1 text-lg">{user.name}</p>
          </div>
          <div>
            <label className="block text-sm font-semibold text-muted-foreground">이메일</label>
            <p className="mt-1 text-lg">{user.email}</p>
          </div>
        </div>
        <div className="mt-8">
          <label
            htmlFor="password"
            className="block text-sm font-semibold text-muted-foreground mb-2"
          >
            회원 탈퇴를 위해 비밀번호 입력
          </label>
          <div className="relative mt-2">
            <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={password}
              required
              className="border border-input pl-10 placeholder:text-primary bg-background"
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
        </div>
        <div className="mt-6 flex flex-col gap-4">
          <Button
            onClick={handleSignOut}
            disabled={loading}
            className="w-full flex justify-center bg-destructive cursor-pointer"
          >
            {loading ? '처리 중...' : '회원 탈퇴'}
          </Button>
          <Button
            className="w-full flex justify-center text-background cursor-pointer"
            onClick={() => router.push('/user/login')}
            disabled={loading}
          >
            로그아웃
          </Button>
        </div>
      </div>
    </RequireAuthenticated>
  );
}
