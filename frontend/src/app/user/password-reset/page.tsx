'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Lock } from 'lucide-react';

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
    <div className="flex flex-col items-center justify-center pt-20">
      <img src="/logo.svg" alt="Coing Logo" className="h-12 mb-6" />

      <h1 className="text-2xl font-bold mb-2">비밀번호 재설정</h1>
      <p className="text-sm mb-6 text-primary">새로운 비밀번호를 입력해 주세요.</p>

      <Card className="w-full max-w-md">
        <CardContent>
          <form className="space-y-4" onSubmit={handlePasswordReset}>
            <div>
              <Label
                htmlFor="newPassword"
                className="block text-sm font-medium text-secondary mb-1"
              >
                새 비밀번호
              </Label>
              <div className="relative mt-2">
                <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="newPassword"
                  name="newPassword"
                  type="password"
                  value={formData.newPassword}
                  onChange={handleInputChange}
                  placeholder="********"
                  required
                  className="border border-gray-300 pl-10 placeholder:text-primary"
                />
              </div>
              {errors.newPassword && (
                <p className="text-red-500 text-sm mt-1">{errors.newPassword}</p>
              )}
            </div>

            <div>
              <Label
                htmlFor="newPasswordConfirm"
                className="block text-sm font-medium text-secondary mb-1"
              >
                비밀번호 확인
              </Label>
              <div className="relative mt-2">
                <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="newPasswordConfirm"
                  name="newPasswordConfirm"
                  type="password"
                  value={formData.newPasswordConfirm}
                  onChange={handleInputChange}
                  placeholder="********"
                  required
                  className="border border-gray-300 pl-10 placeholder:text-primary"
                />
              </div>
              {errors.newPasswordConfirm && (
                <p className="text-red-500 text-sm mt-1">{errors.newPasswordConfirm}</p>
              )}
            </div>

            {errors.general && <p className="text-red-500 text-center">{errors.general}</p>}

            <Button
              type="submit"
              disabled={!isFormValid() || isLoading}
              className="w-full cursor-pointer text-background"
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
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="flex flex-col mt-4">
        <Button
          variant="link"
          className="text-sm text-point cursor-pointer"
          onClick={() => router.push('/user/login')}
        >
          로그인 페이지로 이동
        </Button>
      </div>
    </div>
  );
}
