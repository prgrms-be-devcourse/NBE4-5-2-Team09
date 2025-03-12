'use client';

import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { useAuth } from '@/context/AuthContext';
import Link from 'next/link';

export default function RequireAuthenticated({ children }: { children: React.ReactNode }) {
  const { isLogin } = useAuth();
  if (!isLogin)
    return (
      <div className="flex justify-center items-center w-full px-8 py-12">
        <Card className="p-8 bg-card shadow-sm max-w-md w-full">
          <div className="text-center">
            <p className="text-lg text-muted-foreground font-medium">
              해당 페이지는 로그인 후 이용할 수 있습니다.
            </p>
            <Button className="mt-6 text-card">
              <Link href="/user/login">로그인하러 가기</Link>
            </Button>
          </div>
        </Card>
      </div>
    );

  return <>{children}</>;
}
