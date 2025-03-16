'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

export default function SocialLoginCallbackPage() {
    const router = useRouter();
    const { accessToken, setAccessToken } = useAuth();

    useEffect(() => {
        if (accessToken) {
            router.push('/');
        }
    }, [accessToken, router]);

    useEffect(() => {
        const fetchToken = async () => {
            try {
                const response = await fetch(process.env.NEXT_PUBLIC_API_URL + '/api/auth/refresh', {
                    method: 'POST',
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
                    alert(`로그인 실패: ${errorData.message}`);
                }
            } catch (error) {
                console.error('Login error:', error);
                alert('로그인 중 오류가 발생했습니다.');
            }
        }
        fetchToken();
    }, []);

    return <div>로그인 중...</div>;
}
