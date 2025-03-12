'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@radix-ui/react-label';
import { Mail } from 'lucide-react';

export default function PasswordResetRequestPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  // 요청 성공 여부를 판단하는 상태 (이메일 전송 완료)
  const [emailSent, setEmailSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isLoading) return; // 중복 클릭 방지
    setIsLoading(true);

    try {
      const response = await fetch(
        process.env.NEXT_PUBLIC_API_URL + '/api/auth/password-reset/request',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email }),
        },
      );
      if (response.ok) {
        setMessage('비밀번호 재설정 이메일이 전송되었습니다.');
        setError('');
        setEmailSent(true);
      } else {
        const errorData = await response.json();
        setError(errorData.message || '요청에 실패했습니다.');
        setIsLoading(false); // 오류 시 재시도 가능하도록 버튼 활성화
      }
    } catch {
      setError('서버 오류가 발생했습니다.');
      setIsLoading(false);
    }
  };

  const handleGoToLogin = () => {
    router.push('/user/login');
  };

  return (
    <div className="flex flex-col items-center justify-center pt-10">
      <img src="/logo.svg" alt="Coing Logo" className="h-12 mb-6" />
      <h1 className="text-2xl font-bold mb-2">비밀번호 재설정 요청</h1>
      <p className="text-sm mb-6 text-primary">
        이메일 주소를 입력하시면, 재설정 링크를 보내드립니다.
      </p>

      <Card className="w-full max-w-md">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <Label htmlFor="email" className="block text-sm font-medium text-secondary mb-1">
                이메일 주소
              </Label>
              <div className="relative mt-2">
                <Mail className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="border border-gray-300 pl-10 placeholder:text-primary"
                />
              </div>
            </div>

            {message && <p className="text-primary text-center">{message}</p>}
            {error && <p className="text-destructive text-center">{error}</p>}

            {emailSent ? (
              <Button
                type="button"
                onClick={handleGoToLogin}
                className="w-full py-2 text-background cursor-pointer"
              >
                로그인 페이지로 이동
              </Button>
            ) : (
              <Button
                type="submit"
                disabled={isLoading}
                className={`w-full py-2 text-background ${
                  isLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer '
                }`}
              >
                {isLoading ? '요청 중...' : '요청하기'}
              </Button>
            )}
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
