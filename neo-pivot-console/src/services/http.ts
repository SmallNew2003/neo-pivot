/**
 * 最小 HTTP 客户端封装。
 *
 * @remarks
 * - 统一处理 `Authorization: Bearer <token>`
 * - API Base URL 通过 `VITE_API_BASE_URL` 配置，默认为 `http://localhost:8080`
 */
export async function apiFetch<T>(
  path: string,
  options: RequestInit & { accessToken?: string } = {}
): Promise<T> {
  const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  if (options.accessToken) {
    headers.set("Authorization", `Bearer ${options.accessToken}`);
  }

  const response = await fetch(`${baseUrl}${path}`, { ...options, headers });
  if (!response.ok) {
    const text = await response.text().catch(() => "");
    throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
  }
  return (await response.json()) as T;
}

