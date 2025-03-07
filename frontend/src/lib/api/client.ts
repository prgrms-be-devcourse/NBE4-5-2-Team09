import createClient from "openapi-fetch";
import { paths } from "./generated/schema";

export async function fetchJSON<T>(
  url: string,
  init?: RequestInit
): Promise<T> {
  const res = await fetch(url, init);
  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || "Failed to fetch data");
  }
  return res.json() as Promise<T>;
}

const client = createClient<paths>({
  baseUrl: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export default client;
