'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { MailCheck } from 'lucide-react';

export default function EmailVerificationWaitingScreen() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const userId = searchParams.get('userId');

  // 메시지 & 타이머 상태
  const [message, setMessage] = useState(
    '인증 메일을 보내고 있습니다. 메일 수신까지 몇 분 정도 걸릴 수 있으니 잠시 후 이메일과 스팸함을 확인해 주세요.',
  );
  const [isPolling, setIsPolling] = useState(true);
  const [timeLeft, setTimeLeft] = useState(600); // 10분 (600초)
  const [isResending, setIsResending] = useState(false); // 재전송 버튼 상태

  // 이메일 인증 여부를 폴링
  useEffect(() => {
    if (!userId) {
      setMessage('인증 메일이 발송되었습니다.\n이메일의 링크를 클릭해 인증을 완료해 주세요.');
      setIsPolling(false);
      return;
    }

    const interval = setInterval(async () => {
      console.log('폴링 호출됨');
      try {
        const response = await fetch(
          process.env.NEXT_PUBLIC_API_URL +
            `/api/auth/is-verified?userId=${encodeURIComponent(userId)}`,
          {
            method: 'GET',
            credentials: 'include',
          },
        );
        if (response.ok) {
          const data = await response.json();
          console.log('인증 상태 응답:', data);
          if (data.verified === true) {
            setMessage('이메일 인증이 완료되었습니다. 로그인 화면으로 이동합니다.');
            clearInterval(interval);
            setIsPolling(false);
            router.push('/user/login');
          }
        } else {
          console.error('인증 상태 확인 실패');
        }
      } catch (error) {
        console.error('인증 상태 확인 중 오류 발생:', error);
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [userId, router]);

  // 카운트다운 (10분 타이머)
  useEffect(() => {
    if (timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  // 이메일 재전송 핸들러
  const handleResendEmail = async () => {
    if (!userId || isResending) return; // 중복 요청 방지

    setIsResending(true); // 재전송 요청 중 (버튼 비활성화)

    try {
      const response = await fetch(
        process.env.NEXT_PUBLIC_API_URL +
          `/api/auth/resend-email?userId=${encodeURIComponent(userId)}`,
        {
          method: 'POST',
          credentials: 'include',
        },
      );

      if (response.ok) {
        setMessage('인증 메일이 재전송되었습니다.');
        setTimeLeft(600); // 10분 다시 시작
      } else {
        setMessage('이메일 재전송에 실패했습니다. 다시 시도해 주세요.');
      }
    } catch (error) {
      console.error('이메일 재전송 중 오류 발생:', error);
      setMessage('서버 오류로 인해 이메일을 재전송할 수 없습니다.');
    } finally {
      setIsResending(false); // 요청 완료 후 버튼 활성화
    }
  };

  // 남은 시간 변환 (mm:ss 포맷)
  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
  };

  return (
    <div className="flex flex-col items-center justify-center pt-10">
      <img src="/logo.svg" alt="LOGO" className="h-12 mb-6" />
      <h1 className="text-2xl font-bold mb-2">이메일 인증</h1>
      <p className="text-sm mb-6 text-primary">이메일 인증이 필요합니다</p>

      <Card className="w-full max-w-md">
        <CardContent className="text-center py-8">
          <div className="flex justify-center mb-4">
            <MailCheck className="h-12 w-12 text-primary" />
          </div>
          <p className="text-sm text-muted-foreground mb-6 whitespace-pre-line">{message}</p>
          <p className="text-primary font-semibold mb-6">
            이메일 인증 유효 시간: {formatTime(timeLeft)}
          </p>

          <p className="text-xs text-muted-foreground mb-2">메일을 받지 못하셨나요?</p>
          <Button
            onClick={handleResendEmail}
            disabled={isResending}
            className="w-full mt-2 transition cursor-pointer text-background"
          >
            {isResending ? '재전송 중...' : '인증 메일 재전송'}
          </Button>

          {/* 로딩 표시 */}
          {isPolling && (
            <div className="flex items-center justify-center mt-4">
              <svg
                className="animate-spin h-5 w-5 text-primary"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                ></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
              </svg>
            </div>
          )}
        </CardContent>
      </Card>

      <p className="text-xs text-muted-foreground mt-6 text-center px-4 leading-5">
        인증이 완료되면 자동으로 로그인됩니다.
        <br />
        문제가 있다면{' '}
        <button
          onClick={() => router.push('/user/login')}
          className="underline text-point font-semibold cursor-pointer"
        >
          로그인 화면
        </button>
        으로 이동해주세요.
      </p>
    </div>
  );
}
