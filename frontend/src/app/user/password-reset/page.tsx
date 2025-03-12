'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

export default function PasswordResetPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token'); // URL 쿼리에서 토큰 읽기

  const [formData, setFormData] = useState({
    newPassword: '',
    newPasswordConfirm: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isLoading, setIsLoading] = useState(false);

  // 입력값 변경 시 상태 업데이트 및 유효성 검사
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    validateField(name, value);
  };

  // 각 필드별 유효성 검사
  const validateField = (field: string, value: string) => {
    let errorMsg = '';

    if (field === 'newPassword') {
      // 비밀번호: 8~20자, 숫자, 영문, 특수문자 포함
      if (!/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+]).{8,20}$/.test(value)) {
        errorMsg = '비밀번호는 8~20자이며, 숫자, 영어, 특수문자를 포함해야 합니다.';
      }
    } else if (field === 'newPasswordConfirm') {
      if (!value.trim()) {
        errorMsg = '비밀번호 확인을 입력하세요.';
      } else if (value !== formData.newPassword) {
        errorMsg = '비밀번호가 일치하지 않습니다.';
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

  // 비밀번호 재설정 요청
  const handlePasswordReset = async (e: React.FormEvent) => {
    e.preventDefault();

    // 모든 필드에 대해 재검증
    const newErrors: { [key: string]: string } = {};
    Object.keys(formData).forEach((key) => {
      validateField(key, formData[key as keyof typeof formData]);
      if (errors[key]) {
        newErrors[key] = errors[key];
      }
    });
    setErrors(newErrors);

    if (Object.values(newErrors).some((error) => error)) {
      return;
    }

    if (!token) {
      setErrors((prev) => ({ ...prev, general: '토큰이 유효하지 않습니다.' }));
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/auth/password-reset/confirm?token=${token}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(formData),
        },
      );

      if (response.ok) {
        // 성공 시 로그인 페이지로 리다이렉트하면서 resetCompleted=true 쿼리 파라미터 추가
        router.push('/user/login?resetCompleted=true');
      } else {
        const errorData = await response.json();
        setErrors((prev) => ({
          ...prev,
          general: errorData.message || '비밀번호 재설정에 실패했습니다.',
        }));
      }
    } catch {
      setErrors((prev) => ({ ...prev, general: '서버 오류가 발생했습니다.' }));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
      <div className="mb-6 text-2xl font-bold">LOGO</div>
      <div className="w-full max-w-md bg-white p-8 rounded-md shadow">
        <h1 className="text-2xl font-bold mb-4">비밀번호 재설정</h1>
        <form className="space-y-4" onSubmit={handlePasswordReset}>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">새 비밀번호</label>
            <input
              type="password"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleInputChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
            {errors.newPassword && <p className="text-red-500 text-sm">{errors.newPassword}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호 확인</label>
            <input
              type="password"
              name="newPasswordConfirm"
              value={formData.newPasswordConfirm}
              onChange={handleInputChange}
              className="w-full border border-gray-300 rounded px-3 py-2"
            />
            {errors.newPasswordConfirm && (
              <p className="text-red-500 text-sm">{errors.newPasswordConfirm}</p>
            )}
          </div>
          {errors.general && <p className="text-red-500 text-center">{errors.general}</p>}
          <button
            type="submit"
            disabled={!isFormValid() || isLoading}
            className={`w-full flex justify-center items-center py-2 rounded transition cursor-pointer ${
              isLoading
                ? 'bg-gray-400 text-white cursor-not-allowed'
                : 'bg-blue-600 text-white hover:bg-blue-700'
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
                재설정 중...
              </>
            ) : (
              '비밀번호 재설정'
            )}
          </button>
        </form>
        <div className="mt-4 text-center">
          <button
            onClick={() => router.push('/user/login')}
            className="text-blue-600 font-semibold hover:underline cursor-pointer"
          >
            로그인 페이지로 이동
          </button>
        </div>
      </div>
    </div>
  );
}
