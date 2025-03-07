"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function EmailVerificationWaitingScreen() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const userId = searchParams.get("userId");
    const [message, setMessage] = useState("이메일 인증 대기 중입니다...");
    const [isPolling, setIsPolling] = useState(true);

    useEffect(() => {
        if (!userId) {
            setMessage("인증 메일이 발송되었습니다.\n이메일의 링크를 클릭해 인증을 완료해 주세요.");
            setIsPolling(false);
            return;
        }

        const interval = setInterval(async () => {
            console.log("폴링 호출됨");
            try {
                const response = await fetch(
                    `http://localhost:8080/api/auth/is-verified?userId=${encodeURIComponent(userId)}`,
                    {
                        method: "GET",
                        credentials: "include",
                    }
                );
                if (response.ok) {
                    const data = await response.json();
                    console.log("인증 상태 응답:", data);
                    // 응답이 { verified: true } 형태라고 가정
                    if (data.verified === true) {
                        setMessage("이메일 인증이 완료되었습니다. 로그인 화면으로 이동합니다.");
                        clearInterval(interval);
                        setIsPolling(false);
                        router.push("/user/login");
                    }
                } else {
                    console.error("인증 상태 확인 실패");
                }
            } catch (error) {
                console.error("인증 상태 확인 중 오류 발생:", error);
            }
        }, 3000);

        return () => clearInterval(interval);
    }, [userId, router]);

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-md shadow text-center">
                <h1 className="text-2xl font-bold mb-4">이메일 인증 대기</h1>
                <p className="text-gray-500 mb-6 whitespace-pre-line flex items-center justify-center gap-2">
                    {message}
                    {isPolling && (
                        <svg
                            className="animate-spin h-5 w-5 text-blue-600"
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
                            <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8v8H4z"
                            ></path>
                        </svg>
                    )}
                </p>
            </div>
        </div>
    );
}
