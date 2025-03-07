import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";
import { verifyAccessToken } from "@/lib/auth-helpers";
import client from "@/lib/api/client";
import { components } from "@/lib/api/generated/schema";

export type MarketsResponse = {
  totalPages?: number;
  totalElements?: number;
  first?: boolean;
  last?: boolean;
  size?: number;
  content?: {
    market: string;
    koreanName: string;
    englishName: string;
  }[];
  number?: number;
  sort?: {
    empty?: boolean;
    unsorted?: boolean;
    sorted?: boolean;
  };
  pageable?: {
    offset?: number;
    unpaged?: boolean;
    paged?: boolean;
    pageNumber?: number;
    pageSize?: number;
  };
  numberOfElements?: number;
  empty?: boolean;
};

export async function GET(request: NextRequest) {
  // const accessToken = (await cookies()).get("accessToken");
  // if (!accessToken) {
  //   return NextResponse.json(
  //     { error: "Unauthorized: No token provided" },
  //     { status: 401 }
  //   );
  // }

  // try {
  //   await verifyAccessToken(accessToken.value);
  // } catch (error) {
  //   return NextResponse.json(
  //     { error: "Unauthorized: Invalid or expired token" },
  //     { status: 401 }
  //   );
  // }

  const { searchParams } = new URL(request.url);
  const page = parseInt(searchParams.get("page") || "0", 10);
  const size = parseInt(searchParams.get("size") || "10", 10);
  const response = await client.GET("/api/market", {
    // headers: {
    //   Authorization: `Bearer ${accessToken}`,
    // },
    credentials: "include",
    params: {
      query: {
        page,
        size,
      },
    },
  });

  if (response.error) {
    return NextResponse.json({ error: response["error"] }, { status: 400 });
  }

  const markets = response.data as MarketsResponse;
  return NextResponse.json(markets);
}
