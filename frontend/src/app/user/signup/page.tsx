"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "../../context/auth-context"; // AuthContext 가져오기

export default function SignUpPage() {
    const router = useRouter();
    const { accessToken } = useAuth(); // 로그인 상태 확인
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [passwordConfirm, setPasswordConfirm] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    // ✅ 로그인 상태라면 자동 리다이렉트
    useEffect(() => {
        if (accessToken) {
            router.push("/");
        }
    }, [accessToken, router]);

    const handleSignUp = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== passwordConfirm) {
            alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return;
        }

        setIsLoading(true);

        try {
            const response = await fetch(process.env.NEXT_PUBLIC_API_URL +"/api/auth/signup", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ name, email, password, passwordConfirm }),
            });

            if (response.ok) {
                const data = await response.json();
                // 응답은 UserSignupResponse record 형태로 아래와 같이 오게 됩니다.
                // { message, name, email, userId }
                alert(data.message || "회원가입 성공. 인증 이메일 전송 완료.");

                if (data.userId) {
                    router.push(`/user/email-verification?userId=${data.userId}`);
                } else {
                    alert("회원가입 응답에서 userId를 찾을 수 없습니다.");
                    setIsLoading(false);
                }
            } else {
                const errorData = await response.json();
                alert(`회원가입 실패: ${errorData.message || "에러 발생"}`);
                setIsLoading(false);
            }
        } catch (error) {
            console.error("SignUp error:", error);
            alert("회원가입 중 오류가 발생했습니다.");
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="mb-6 text-2xl font-bold">LOGO</div>
            <div className="w-full max-w-md bg-white p-8 rounded-md shadow">
                <h1 className="text-2xl font-bold mb-4">회원가입</h1>
                <form className="space-y-4" onSubmit={handleSignUp}>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            이름
                        </label>
                        <input
                            type="text"
                            required
                            placeholder="홍길동"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            이메일
                        </label>
                        <input
                            type="email"
                            required
                            placeholder="example@email.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            비밀번호
                        </label>
                        <input
                            type="password"
                            required
                            placeholder="********"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            비밀번호 확인
                        </label>
                        <input
                            type="password"
                            required
                            placeholder="********"
                            value={passwordConfirm}
                            onChange={(e) => setPasswordConfirm(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={isLoading}
                        className={`w-full flex justify-center items-center py-2 rounded transition ${
                            isLoading
                                ? "bg-gray-400 text-white cursor-not-allowed"
                                : "bg-blue-600 text-white hover:bg-blue-700"
                        }`}
                    >
                        {isLoading ? (
                            <>
                                <svg
                                    className="animate-spin h-5 w-5 mr-2 text-white"
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
                                회원가입 중...
                            </>
                        ) : (
                            "회원가입"
                        )}
                    </button>
                </form>

                {/* ✅ 로그인 버튼 추가 */}
                <div className="mt-4 text-center">
                    <span className="text-gray-600 text-sm">이미 계정이 있으신가요?</span>
                    <button
                        onClick={() => router.push("/user/login")}
                        className="ml-2 text-blue-600 font-semibold hover:underline"
                    >
                        로그인
                    </button>
                </div>
            </div>
        </div>
    );
}
