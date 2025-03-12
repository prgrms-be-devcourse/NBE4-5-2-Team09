'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function PasswordResetRequestPage() {
    const router = useRouter();
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    // 요청 성공 여부를 판단하는 상태 (이메일 전송 완료)
    const [emailSent, setEmailSent] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (isLoading) return; // 중복 클릭 방지
        setIsLoading(true);

        try {
            const response = await fetch(
                process.env.NEXT_PUBLIC_API_URL + '/api/auth/password-reset/request',
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email }),
                }
            );
            if (response.ok) {
                setMessage('비밀번호 재설정 이메일이 전송되었습니다.');
                setError('');
                setEmailSent(true);
            } else {
                const errorData = await response.json();
                setError(errorData.message || '요청에 실패했습니다.');
                setIsLoading(false); // 오류 시 재시도 가능하도록 버튼 활성화
            }
        } catch (err) {
            setError('서버 오류가 발생했습니다.');
            setIsLoading(false);
        }
    };

    const handleGoToLogin = () => {
        router.push('/user/login');
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-md shadow">
                <h1 className="text-2xl font-bold mb-4">비밀번호 재설정 요청</h1>
                <form className="space-y-4" onSubmit={handleSubmit}>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            이메일 주소
                        </label>
                        <input
                            type="email"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>
                    {message && <p className="text-green-600 text-center">{message}</p>}
                    {error && <p className="text-red-600 text-center">{error}</p>}
                    {/* 버튼을 emailSent 상태에 따라 분기 처리 */}
                    {emailSent ? (
                        <button
                            type="button"
                            onClick={handleGoToLogin}
                            className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700 cursor-pointer"
                        >
                            로그인 페이지로 이동
                        </button>
                    ) : (
                        <button
                            type="submit"
                            disabled={isLoading}
                            className={`w-full bg-blue-600 text-white py-2 rounded ${
                                isLoading
                                    ? 'opacity-50 cursor-not-allowed'
                                    : 'hover:bg-blue-700 cursor-pointer'
                            }`}
                        >
                            {isLoading ? '요청 중...' : '요청하기'}
                        </button>
                    )}
                </form>
            </div>
        </div>
    );
}
