# 0007：统一 API 响应体（方案 B：成功/失败统一 Envelope）提案

## 背景与问题陈述

当前后端接口返回风格不统一：

- 成功响应直接返回 DTO（例如 `LoginResponse`、`DocumentDto`、`PresignResponse`）
- 失败响应主要依赖 `@ResponseStatus` 异常返回 HTTP 状态码，响应体结构由默认错误机制决定（或为空）

这会导致前端/调用方需要分别处理：

- “正常数据 JSON”与“错误 JSON/纯文本/空响应”的多种形态
- 鉴权失败（401/403）与业务异常在响应结构上的差异

随着接口数量增长，这类差异会增加联调与错误处理成本，因此需要在进入规模化功能实现前统一对外 API 的响应契约。

## 目标（Goals）

- 所有业务接口（`/api/**`）的成功与失败响应统一返回 Envelope 结构。
- HTTP 状态码保留语义（401/403/404/409/500 等），Envelope 用于承载稳定的 `code/message` 与可选 `details`。
- 通过统一机制实现：避免 Controller 手工包一层，降低侵入性。
- 安全层（未认证/无权限）返回与业务异常一致的 Envelope。

## 非目标（Non-Goals）

- 本提案不定义完整的“业务错误码体系”（仅定义最小可用的一组 `code` 约定，后续可扩展）。
- 本提案不调整业务逻辑，不新增业务接口。
- 本提案不要求将所有非 JSON 场景（文件下载/流式响应）纳入 Envelope；此类场景按需豁免。

## 范围（Scope）

### 适用范围

- 后端：`neo-pivot-server` 中 `com.jelvin.neopivot` 包下的业务 Controller（主要是 `/api/**`）
- 前端：`neo-pivot-console` 的 HTTP 客户端封装需同步适配 Envelope（否则会因结构变化而解析失败）

### 豁免范围

- 文件下载、二进制流、SSE 等非 JSON 语义响应（若未来引入，将在实现中显式跳过包装）
- SpringDoc/Knife4j/Actuator 等框架自带端点（不在业务包范围内）

## 方案概述

### 1) Envelope 结构（对外契约）

统一响应结构如下（字段顺序不做强约束）：

成功响应：

```json
{
  "success": true,
  "code": "0",
  "message": "OK",
  "data": {},
  "traceId": "2c8a3b6bb5b54d5a8d1c2e6e1d1d2f3a"
}
```

失败响应：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "data": null,
  "traceId": "2c8a3b6bb5b54d5a8d1c2e6e1d1d2f3a",
  "details": [
    { "field": "username", "message": "不能为空" }
  ]
}
```

字段定义：

- `success`：是否成功
- `code`：稳定错误码；成功固定为 `"0"`
- `message`：可读错误信息；成功固定为 `"OK"`（或保持默认）
- `data`：成功数据载体；失败为 `null`
- `traceId`：请求追踪标识（来自请求头或服务端生成），同时回写到响应头便于排查
- `details`：可选，主要用于参数校验错误等结构化错误信息

### 2) 最小错误码集合（初版）

初版建议最小集合如下（后续可扩展为业务域错误码）：

- `0`：成功
- `BAD_REQUEST`：400（通用请求错误）
- `VALIDATION_ERROR`：400（参数校验失败）
- `UNAUTHORIZED`：401（未认证）
- `FORBIDDEN`：403（无权限）
- `NOT_FOUND`：404（资源不存在）
- `CONFLICT`：409（资源冲突/重复提交）
- `GONE`：410（资源过期）
- `NOT_IMPLEMENTED`：501（骨架占位/未实现）
- `INTERNAL_ERROR`：500（未预期异常）

### 3) 后端实现策略（低侵入）

- 使用响应体包装器（ResponseBodyAdvice）对业务 Controller 的成功返回进行自动 Envelope 包装。
- 使用全局异常处理（RestControllerAdvice）将常见异常转换为 Envelope，并保留合适的 HTTP 状态码。
- 安全层通过自定义 `AuthenticationEntryPoint` 与 `AccessDeniedHandler` 输出 Envelope，统一 401/403 的响应结构。
- 引入轻量 `traceId` 生成/透传机制：优先从 `X-Request-Id` 读取，否则生成并回写。

### 4) 前端适配策略

- 更新前端 `apiFetch`：对成功响应自动解包 `data`；对失败响应提取 `message/code` 并抛出可读错误。
- 保持业务调用点不变（仍然 `apiFetch<T>()` 返回 `T`），将结构变化收敛在 HTTP 客户端层。

## 里程碑（Milestones）

1. 后端：实现 Envelope 结构、成功自动包装、全局异常统一输出
2. 后端：补齐安全层 401/403 的 Envelope 输出
3. 前端：适配 `apiFetch` 解包逻辑，确保登录与业务请求可继续运行

## 风险与对策

- 风险：对前端/调用方是破坏性变更（DTO 外层增加 Envelope）
  - 对策：同步更新前端 `apiFetch`，并在提案中明确迁移步骤
- 风险：某些特殊响应类型（文件/流）被错误包装
  - 对策：实现中显式跳过非 JSON/二进制类型响应
- 风险：错误消息来源不一致（部分异常无 message）
  - 对策：对常见 HTTP 状态码提供默认中文 `message` 兜底

## 验收标准（Acceptance Criteria）

- `/api/**` 成功响应统一返回 Envelope，且 `success=true`、`code="0"`、`data` 为原有 DTO
- `/api/**` 失败响应统一返回 Envelope，且 HTTP 状态码语义正确
- 401/403 返回 Envelope（不再是默认空响应或 HTML）
- 前端 `neo-pivot-console` 登录接口与至少一个业务接口调用可正确解析响应

## 开放问题（Open Questions）

- 是否需要在初版就引入更细粒度的业务错误码（例如 `DOC_PRESIGN_EXPIRED`）？
- `message` 的国际化/多语言是否需要在后续支持？

