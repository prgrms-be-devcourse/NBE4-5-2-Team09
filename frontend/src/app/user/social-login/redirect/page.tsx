'use client';

import { useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

export default function SocialLoginRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { accessToken, setAccessToken } = useAuth();
  const hasFetched = useRef(false);

  useEffect(() => {
    if (accessToken) {
      router.push('/');
    }
  }, [accessToken, router]);

  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;

    const tempToken = searchParams.get('tempToken'); // URL에서 tempToken 가져오기

    if (!tempToken) {
      alert('유효하지 않은 로그인 요청입니다.');
      router.push('/');
      return;
    }

    const fetchAccessToken = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/auth/social-login/redirect?tempToken=${tempToken}`,
          {
            method: 'POST',
            credentials: 'include',
          },
        );

        if (response.ok) {
          const data = await response.json();
          console.log('소셜 로그인 성공:', data);

          const authHeader = response.headers.get('Authorization');
          if (authHeader?.startsWith('Bearer ')) {
            const token = authHeader.slice(7);
            setAccessToken(token);
            console.log('Access Token 저장됨:', token);
          }
          router.push('/');
        } else {
          const errorData = await response.json();
          alert(`로그인 실패: ${errorData.message}`);
          router.push('/');
        }
      } catch (error) {
        console.error('Login error:', error);
        alert('로그인 중 오류가 발생했습니다.');
        router.push('/');
      }
    };

    fetchAccessToken();
  }, [searchParams, router, setAccessToken]);

  return <div>로그인 중...</div>;
}
