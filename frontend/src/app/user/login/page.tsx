'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Mail, Lock } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const { accessToken, setAccessToken } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  useEffect(() => {
    if (accessToken) {
      router.push('/');
    }
  }, [accessToken, router]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const response = await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
        credentials: 'include',
      });

      if (response.ok) {
        const authHeader = response.headers.get('Authorization');
        if (authHeader?.startsWith('Bearer ')) {
          const token = authHeader.slice(7);
          setAccessToken(token);
          console.log('Access Token 저장됨:', token);
        }
        router.push('/');
      } else {
        const errorData = await response.json();
        // 미인증 사용자라면 백엔드에서 userId를 detail 필드에 포함했다고 가정합니다.
        if (errorData.message === '이메일 인증이 완료되지 않았습니다.') {
          const userId = errorData.detail;
          if (userId) {
            router.push(`/user/email/email-verification?userId=${userId}`);
          } else {
            router.push('/user/email/email-verification');
          }
        } else {
          alert(`로그인 실패: ${errorData.message}`);
        }
      }
    } catch (error) {
      console.error('Login error:', error);
      alert('로그인 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center pt-20">
      <img src="/logo.svg" alt="Coing Logo" className="h-12 mb-6" />

      <h1 className="text-2xl font-bold mb-2">환영합니다</h1>
      <p className="text-sm mb-6 text-primary">서비스를 이용하시려면 로그인해 주세요.</p>

      <Card className="w-full max-w-md">
        <CardContent>
          <form className="space-y-4" onSubmit={handleLogin}>
            <div>
              <Label htmlFor="email" className="block text-sm font-medium text-secondary mb-1">
                이메일 주소
              </Label>
              <div className="relative mt-2">
                <Mail className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                />
              </div>
            </div>
            <div>
              <Label htmlFor="password" className="mb-1 pt-4">
                비밀번호
              </Label>
              <div className="relative mt-2">
                <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="password"
                  type="password"
                  placeholder="********"
                  value={password}
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>
            <Button type="submit" className="w-full mt-4 text-background cursor-pointer">
              로그인
            </Button>
          </form>
          <Button
            className="w-full mt-4 bg-muted text-foreground cursor-pointer hover:bg-background"
            onClick={() => router.push('/user/social-login')}
          >
            간편 로그인
          </Button>
        </CardContent>
      </Card>

      <div className="flex flex-col mt-4">
        <Button
          variant="link"
          className="text-sm text-point cursor-pointer"
          onClick={() => router.push('/user/password-reset/request')}
        >
          비밀번호를 잊으셨나요?
        </Button>

        <div className="text-center text-sm">
          <span className="text-primary">아직 회원이 아니신가요?</span>
          <Button
            variant="link"
            onClick={() => router.push('/user/signup')}
            className="ml-2 p-0 text-sm text-point cursor-pointer"
          >
            회원가입
          </Button>
        </div>
      </div>
    </div>
  );
}
