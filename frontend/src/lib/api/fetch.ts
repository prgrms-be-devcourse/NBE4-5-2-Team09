export async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, options);
  if (!response.ok) {
    throw new Error(`HTTP error! Status: ${response.status}`);
  }
  return (await response.json()) as T;
}
