// utils/parse-token.ts

export function parseJwt(token: string | null | undefined): Record<string, any> | null {
    if (!token) return null;
    try {
        const parts = token.split(".");
        if (parts.length < 2) return null;

        // header = parts[0], payload = parts[1]
        const base64Payload = parts[1]
            // base64url -> base64 변환 (URL 안전 문자 '-' '_')
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        // ---- 유니코드 대응 디코딩 ----
        const jsonPayload = decodeBase64Unicode(base64Payload);
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error("JWT 파싱 에러:", error);
        return null;
    }
}

/**
 * Base64 -> 유니코드 문자열로 디코딩
 */
function decodeBase64Unicode(str: string): string {
    // 1) atob로 디코딩 (ASCII)
    // 2) 디코딩 결과를 각 바이트(charCode)별 %xx 형태로 변환
    // 3) decodeURIComponent로 유니코드 복원
    const ascii = atob(str);
    const length = ascii.length;
    let result = "";
    for (let i = 0; i < length; i++) {
        const code = ascii.charCodeAt(i);
        result += "%" + ("00" + code.toString(16)).slice(-2);
    }
    return decodeURIComponent(result);
}
