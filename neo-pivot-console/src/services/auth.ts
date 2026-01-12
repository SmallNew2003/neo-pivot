import { apiFetch } from "./http";

/**
 * 认证服务（前端）。
 *
 * @remarks
 * - MVP 使用 localStorage 持久化 token 以便刷新页面后可继续使用
 * - 如需更高安全性，可改为仅内存存储 + 更短 token TTL
 */
const ACCESS_TOKEN_KEY = "neo-pivot.accessToken";

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
}

export function clearAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
}

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresInSeconds?: number;
  user?: { id: string; username: string; roles: string[] };
};

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  setAccessToken(response.accessToken);
  return response;
}

