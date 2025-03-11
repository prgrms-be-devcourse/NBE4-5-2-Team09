"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function EmailVerificationWaitingScreen() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const userId = searchParams.get("userId");

    // 메시지 & 타이머 상태
    const [message, setMessage] = useState(
        "인증 메일을 보내고 있습니다. \n 메일 수신까지 시간이 걸릴 수 있습니다."
    );
    const [isPolling, setIsPolling] = useState(true);
    const [timeLeft, setTimeLeft] = useState(600); // 10분 (600초)
    const [isResending, setIsResending] = useState(false); // 재전송 버튼 상태

    // 이메일 인증 여부를 폴링
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
                    process.env.NEXT_PUBLIC_API_URL +
                    `/api/auth/is-verified?userId=${encodeURIComponent(userId)}`,
                    {
                        method: "GET",
                        credentials: "include",
                    }
                );
                if (response.ok) {
                    const data = await response.json();
                    console.log("인증 상태 응답:", data);
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
                    method: "POST",
                    credentials: "include",
                }
            );

            if (response.ok) {
                setMessage("인증 메일이 재전송되었습니다.");
                setTimeLeft(600); // 10분 다시 시작
            } else {
                setMessage("이메일 재전송에 실패했습니다. 다시 시도해 주세요.");
            }
        } catch (error) {
            console.error("이메일 재전송 중 오류 발생:", error);
            setMessage("서버 오류로 인해 이메일을 재전송할 수 없습니다.");
        } finally {
            setIsResending(false); // 요청 완료 후 버튼 활성화
        }
    };

    // 남은 시간 변환 (mm:ss 포맷)
    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes}:${secs < 10 ? "0" : ""}${secs}`;
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-md shadow text-center">
                <h1 className="text-2xl font-bold mb-4">이메일 인증 대기</h1>
                {/* 안내 문구 */}
                <p className="text-gray-500 mb-4 whitespace-pre-line">{message}</p>
                {/* 타이머 */}
                <p className="text-blue-600 font-semibold mb-4">
                    이메일 인증 유효 시간: {formatTime(timeLeft)}
                </p>
                {/* 이메일 재전송 버튼 */}
                <button
                    onClick={handleResendEmail}
                    disabled={isResending}
                    className={`w-full py-2 mt-2 rounded transition ${
                        isResending
                            ? "bg-gray-400 text-white cursor-not-allowed"
                            : "bg-blue-600 text-white hover:bg-blue-700"
                    }`}
                >
                    {isResending ? "재전송 중..." : "이메일 재전송"}
                </button>
                {/* 로딩 표시 */}
                {isPolling && (
                    <div className="flex items-center justify-center mt-4">
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
                    </div>
                )}
            </div>
        </div>
    );
}
