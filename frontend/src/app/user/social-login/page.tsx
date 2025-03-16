'use client';

import { Button } from "@/components/ui/button";
import { Card, CardContent } from '@/components/ui/card';
import { useRouter } from 'next/navigation';

export default function SocialLoginPage() {
    const router = useRouter();

    const handleKakaoLogin = () => {
        window.location.href = `${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/kakao`;
    };

    return (
        <div className="flex flex-col items-center justify-center pt-20">
            <img src="/logo.svg" alt="Coing Logo" className="h-12 mb-6" />

            <h1 className="text-2xl font-bold mb-2">환영합니다</h1>
            <p className="text-sm mb-6 text-primary">서비스를 이용하시려면 로그인해 주세요.</p>

            <Card className="w-full max-w-md">
                <CardContent>
                    <button
                        onClick={handleKakaoLogin}
                        className="w-full max-w-md flex justify-center cursor-pointer"
                    >
                        <img src="/kakao_login_medium_wide.png" alt="카카오 로그인" className="w-full max-w-xs" />
                    </button>
                </CardContent>
            </Card>

            <div className="flex flex-col mt-4">
                <Button
                    variant="link"
                    className="text-sm text-point cursor-pointer"
                    onClick={() => router.push('/user/login')}
                >
                    이메일로 로그인 하시겠습니까?
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