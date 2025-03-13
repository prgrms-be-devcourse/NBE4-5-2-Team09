'use client';

import { defaultState, useUserStore } from '@/store/user.store';
import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';

interface AuthContextType {
  isLogin: boolean;
  accessToken: string | null;
  setAccessToken: (token: string | null) => void;
  isAuthLoading: boolean;
  customFetch: (input: RequestInfo, init?: RequestInit) => Promise<Response>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  // 세션 스토리지에서 accessToken 초기 로드
  const [accessToken, setAccessTokenState] = useState<string | null>(() => {
    if (typeof window !== 'undefined') {
      const token = sessionStorage.getItem('accessToken') || null;
      console.log('[AuthProvider] 초기 accessToken:', token);
      return token;
    }
    return null;
  });
  const [isAuthLoading, setIsAuthLoading] = useState<boolean>(true);
  const { setUser } = useUserStore();

  const setAccessToken = (token: string | null) => {
    setAccessTokenState(token);
    if (typeof window !== 'undefined') {
      if (token) {
        sessionStorage.setItem('accessToken', token);
      } else {
        sessionStorage.removeItem('accessToken');
      }
    }
  };

  // 토큰 재발급 함수
  async function refreshAccessToken(): Promise<string | null> {
    console.log('[AuthProvider] refreshAccessToken 호출');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
      });
      if (response.ok) {
        const authHeader = response.headers.get('Authorization');
        if (authHeader?.startsWith('Bearer ')) {
          const newToken = authHeader.slice(7);
          setAccessToken(newToken);
          return newToken;
        }
      }
    } catch (error) {
      console.error('[AuthProvider] 리프레시 요청 중 오류 발생:', error);
    }
    return null;
  }

  // customFetch: 모든 API 요청 시 토큰을 헤더에 포함하고, 403 발생 시 재요청
  const customFetch = useCallback(
    async (input: RequestInfo, init?: RequestInit): Promise<Response> => {
      let headers = {
        ...(init?.headers || {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      };
      let response = await fetch(input, { ...init, headers, credentials: 'include' });

      if (response.status === 403) {
        const newToken = await refreshAccessToken();
        if (newToken) {
          headers = { ...init?.headers, Authorization: `Bearer ${newToken}` };
          response = await fetch(input, { ...init, headers, credentials: 'include' });
        } else {
          window.location.href = '/login';
        }
      }
      return response;
    },
    [accessToken],
  );

  // 컴포넌트 마운트 시 토큰 유무 체크
  useEffect(() => {
    async function validateToken() {
      const token = sessionStorage.getItem('accessToken');
      setIsAuthLoading(false);
    }
    validateToken();
  }, []);

  // accessToken이 있을 때 사용자 정보를 한 번만 API 호출로 가져와 전역 스토어에 저장
  useEffect(() => {
    async function fetchUserInfo() {
      if (!accessToken) {
        setUser(defaultState);
        return;
      }
      try {
        const res = await customFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/info`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
        });
        if (res.ok) {
          const data = await res.json();
          setUser({ name: data.name, email: data.email });
        } else {
          setUser(defaultState);
        }
      } catch (error) {
        console.error('[AuthProvider] 사용자 정보 요청 실패:', error);
        setUser(defaultState);
      }
    }
    fetchUserInfo();
  }, [accessToken, setUser]);

  return (
    <AuthContext.Provider
      value={{
        isLogin: !!accessToken,
        accessToken,
        setAccessToken,
        isAuthLoading,
        customFetch,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내에서 사용해야 합니다.');
  }
  return context;
}
