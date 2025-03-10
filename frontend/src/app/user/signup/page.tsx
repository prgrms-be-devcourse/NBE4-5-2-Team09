"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "../../context/auth-context";

export default function SignUpPage() {
    const router = useRouter();
    const { accessToken } = useAuth();

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        passwordConfirm: "",
    });

    const [errors, setErrors] = useState<{ [key: string]: string }>({});
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (accessToken) {
            router.push("/");
        }
    }, [accessToken, router]);

    // 입력값이 변경될 때마다 상태 업데이트 & 즉시 유효성 검사
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        validateField(name, value);
    };

    // 실시간 유효성 검사 함수
    const validateField = (field: string, value: string) => {
        let errorMsg = "";

        if (field === "name") {
            if (value.trim().length < 2 || value.trim().length > 20) {
                errorMsg = "이름은 2~20자 사이여야 합니다.";
            }
        } else if (field === "email") {
            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
                errorMsg = "유효한 이메일을 입력하세요.";
            }
        } else if (field === "password") {
            if (!/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+]).{8,20}$/.test(value)) {
                errorMsg = "비밀번호는 8~20자이며, 숫자, 영어, 특수문자를 포함해야 합니다.";
            }
        } else if (field === "passwordConfirm") {
            if (!value.trim()) {
                errorMsg = "비밀번호 확인을 입력하세요.";
            } else if (value !== formData.password) {
                errorMsg = "비밀번호가 일치하지 않습니다.";
            }
        }

        setErrors((prev) => ({ ...prev, [field]: errorMsg }));
    };

    // 모든 필드가 유효한지 확인하는 함수
    const isFormValid = () => {
        return (
            Object.values(errors).every((error) => !error) &&
            Object.values(formData).every((value) => value.trim())
        );
    };

    // 회원가입 요청
    const handleSignUp = async (e: React.FormEvent) => {
        e.preventDefault();

        // 모든 필드에 대한 유효성 검사 실행
        const newErrors: { [key: string]: string } = {};
        Object.keys(formData).forEach((key) => {
            validateField(key, formData[key as keyof typeof formData]);
            if (errors[key]) {
                newErrors[key] = errors[key]; // 기존 오류 메시지 유지
            }
        });

        // 유효성 검사 결과 업데이트
        setErrors(newErrors);

        // 유효성 검사를 통과하지 못하면 요청 중단
        if (Object.values(newErrors).some((error) => error)) {
            return;
        }

        setIsLoading(true);

        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/signup`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                const data = await response.json();
                router.push(`/user/email/email-verification?userId=${data.userId}`);
            } else {
                const errorData = await response.json();
                setErrors({ ...newErrors, general: errorData.message || "회원가입 실패" });
            }
        } catch (error) {
            setErrors({ ...newErrors, general: "서버 오류가 발생했습니다." });
        } finally {
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
                        <label className="block text-sm font-medium text-gray-700 mb-1">이름</label>
                        <input
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                        {errors.name && <p className="text-red-500 text-sm">{errors.name}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                        {errors.email && <p className="text-red-500 text-sm">{errors.email}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                        {errors.password && <p className="text-red-500 text-sm">{errors.password}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호 확인</label>
                        <input
                            type="password"
                            name="passwordConfirm"
                            value={formData.passwordConfirm}
                            onChange={handleInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                        {errors.passwordConfirm && <p className="text-red-500 text-sm">{errors.passwordConfirm}</p>}
                    </div>

                    {errors.general && <p className="text-red-500 text-center">{errors.general}</p>}

                    <button
                        type="submit"
                        disabled={!isFormValid() || isLoading}
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
