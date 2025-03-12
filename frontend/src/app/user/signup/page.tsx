'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { User, Mail, Lock } from 'lucide-react';
import { Label } from '@/components/ui/label';

export default function SignUpPage() {
  const router = useRouter();
  const { accessToken } = useAuth();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    passwordConfirm: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (accessToken) {
      router.push('/');
    }
  }, [accessToken, router]);

  // 입력값이 변경될 때마다 상태 업데이트 & 즉시 유효성 검사
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => {
      const newErrors = { ...prev };
      if (newErrors.general) {
        delete newErrors.general;
      }
      return newErrors;
    });
    validateField(name, value);
  };

  // 실시간 유효성 검사 함수
  const validateField = (field: string, value: string) => {
    let errorMsg = '';

    if (field === 'name') {
      if (value.trim().length < 2 || value.trim().length > 20) {
        errorMsg = '이름은 2~20자 사이여야 합니다.';
      }
    } else if (field === 'email') {
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        errorMsg = '유효한 이메일을 입력하세요.';
      }
    } else if (field === 'password') {
      if (!/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+]).{8,20}$/.test(value)) {
        errorMsg = '비밀번호는 8~20자이며, 숫자, 영어, 특수문자를 포함해야 합니다.';
      }
    } else if (field === 'passwordConfirm') {
      if (!value.trim()) {
        errorMsg = '비밀번호 확인을 입력하세요.';
      } else if (value !== formData.password) {
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
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const data = await response.json();
        router.push(`/user/email/email-verification?userId=${data.userId}`);
      } else {
        const errorData = await response.json();
        setErrors({
          ...newErrors,
          general: errorData.message || '회원가입 실패',
        });
      }
    } catch {
      setErrors({ ...newErrors, general: '서버 오류가 발생했습니다.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center pt-10">
      <img src="/logo.svg" alt="Coing Logo" className="h-12 mb-6" />
      <h1 className="text-2xl font-bold mb-2">회원가입</h1>
      <p className="text-sm mb-6 text-primary">서비스 이용을 위해 회원가입을 진행해 주세요.</p>

      <Card className="w-full max-w-md">
        <CardContent>
          <form className="space-y-4" onSubmit={handleSignUp}>
            <div>
              <Label htmlFor="name" className="block text-sm font-medium text-secondary mb-1">
                이름
              </Label>
              <div className="relative mt-2">
                <User className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="name"
                  name="name"
                  type="text"
                  value={formData.name}
                  onChange={handleInputChange}
                  placeholder="홍길동"
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                />
              </div>
              {errors.name && <p className="text-red-500 text-sm mt-1">{errors.name}</p>}
            </div>
            <div>
              <Label htmlFor="email" className="block text-sm font-medium text-secondary mb-1">
                이메일
              </Label>
              <div className="relative mt-2">
                <Mail className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="name@example.com"
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                />
              </div>
              {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
            </div>
            <div>
              <Label htmlFor="password" className="block text-sm font-medium text-secondary mb-1">
                비밀번호
              </Label>
              <div className="relative mt-2">
                <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="********"
                  value={formData.password}
                  onChange={handleInputChange}
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                />
              </div>
              {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
            </div>
            <div className="pb-4">
              <Label
                htmlFor="passwordConfirm"
                className="block text-sm font-medium text-secondary mb-1"
              >
                비밀번호 확인
              </Label>
              <div className="relative mt-2">
                <Lock className="absolute left-3 top-2.5 h-4 w-4 text-primary" />
                <Input
                  id="passwordConfirm"
                  name="passwordConfirm"
                  type="password"
                  placeholder="********"
                  value={formData.passwordConfirm}
                  onChange={handleInputChange}
                  required
                  className="border border-input pl-10 placeholder:text-primary bg-background"
                />
              </div>
              {errors.passwordConfirm && (
                <p className="text-red-500 text-sm mt-1">{errors.passwordConfirm}</p>
              )}
            </div>

            {errors.general && <p className="text-red-500 text-center">{errors.general}</p>}

            <Button
              type="submit"
              disabled={!isFormValid() || isLoading}
              className="w-full flex justify-center text-background cursor-pointer"
            >
              {isLoading ? (
                <>
                  <svg
                    className="animate-spin h-5 w-5 mr-2 text-card-foreground"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle className="opacity-25" cx="12" cy="12" r="10" strokeWidth="4"></circle>
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v8H4z"
                    ></path>
                  </svg>
                  회원가입 중...
                </>
              ) : (
                '회원가입'
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="flex flex-col mt-4 items-center">
        <div className="text-center text-sm">
          <span className="text-primary">이미 계정이 있으신가요?</span>
          <Button
            variant="link"
            onClick={() => router.push('/user/login')}
            className="ml-2 p-0 text-sm text-point cursor-pointer"
          >
            로그인
          </Button>
        </div>
      </div>
    </div>
  );
}
