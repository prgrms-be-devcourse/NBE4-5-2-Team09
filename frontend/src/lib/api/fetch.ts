export async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  // sessionStorage에서 accessToken을 가져옵니다.
  const accessToken = typeof window !== 'undefined' ? sessionStorage.getItem('accessToken') : null;

  // 기본 헤더 설정 (accessToken이 있으면 Authorization 헤더 추가)
  const defaultHeaders: HeadersInit = {
    'Content-Type': 'application/json',
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
  };

  // 기본 옵션: 쿠키를 포함하도록 credentials: 'include' 설정
  const defaultOptions: RequestInit = {
    credentials: 'include',
    headers: defaultHeaders,
  };

  const mergedOptions: RequestInit = {
    ...defaultOptions,
    ...options,
    headers: {
      ...defaultHeaders,
      ...options?.headers,
    },
  };

  const response = await fetch(url, mergedOptions);
  if (!response.ok) {
    throw new Error(`HTTP error! Status: ${response.status}`);
  }
  return (await response.json()) as T;
}
