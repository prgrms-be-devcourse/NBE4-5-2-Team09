"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";

export default function EmailVerificationPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const [message, setMessage] = useState<string>("이메일 인증 확인 중...");
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const token = searchParams.get("token");
        if (!token) {
            setMessage("인증 토큰이 제공되지 않았습니다.");
            setLoading(false);
            return;
        }

        // ✅ 이메일 인증 API 호출
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/verify-email?token=${token}`)
            .then(async (res) => {
                const data = await res.json();
                setLoading(false);

                if (data.status === "success") {
                    setMessage("이메일 인증이 완료되었습니다. 로그인 페이지로 이동합니다...");
                    setTimeout(() => {
                        router.push("/user/login?verified=true");
                    }, 2000);
                } else if (data.status === "already") {
                    setMessage("이미 인증된 사용자입니다. 로그인 페이지로 이동합니다...");
                    setTimeout(() => {
                        router.push("/user/login");
                    }, 2000);
                } else {
                    setMessage("이메일 인증에 실패했습니다.");
                    setTimeout(() => {
                        router.push("/user/email-verification-failed");
                    }, 2000);
                }
            })
            .catch((error) => {
                console.error("인증 에러:", error);
                setMessage("이메일 인증 중 오류가 발생했습니다.");
                setLoading(false);
            });
    }, [searchParams, router]);

    return (
        <div className="flex items-center justify-center min-h-screen bg-white">
            <div className="text-center p-6 shadow-md rounded-md border border-gray-200 bg-white w-96">
                <h1 className="text-2xl font-bold mb-4">이메일 인증</h1>
                {loading ? (
                    <p className="text-gray-600">이메일 인증을 진행 중입니다...</p>
                ) : (
                    <p className="text-gray-800">{message}</p>
                )}
            </div>
        </div>
    );
}
