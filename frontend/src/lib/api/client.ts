import createClient from "openapi-fetch";
import { paths } from "@/lib/api/generated/schema";

const client = createClient<paths>({
  baseUrl: process.env.NEXT_PUBLIC_WEBSOCKET_URL, // NEXT_PUBLIC_ 접두사 사용
  headers: {
    "Content-Type": "application/json",
  },
});

export default client;
