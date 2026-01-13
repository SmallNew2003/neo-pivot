# 0003：标准登录与 JWT 签发规格（Spec）

> 本规格用于支撑前端管理台的“标准登录”与平台接入方案A（用户级 JWT 透传）。

## 1. 目标与范围

### 1.1 目标

- 提供标准登录能力：用户名/密码 → 签发用户级 JWT（RS256）。
- 资源服务（核心底座 API）统一以 JWT 验签并解析 `sub/roles` 实现权限隔离（`owner_id == userId(sub)`）。
- 支持 JWKS 公钥发布，便于资源服务验签与密钥轮换。

### 1.2 非目标（MVP 不做）

- 不做完整 OAuth2 授权码流程与第三方登录。
- 不做多因素认证（MFA）。
- 不做复杂的权限后台（仅提供最小 roles）。
- MVP 默认不提供 refresh token（后续可扩展）。

## 2. 术语

- **认证服务（Auth Issuer）**：负责校验用户名/密码并签发 JWT 的组件（可与底座同进程部署）。
- **资源服务（Resource Server）**：负责验签并执行业务 API 的组件（底座对外 API）。

> MVP 可合并部署在同一个 Spring Boot 应用中，但职责需在代码结构上保持清晰。

## 3. JWT 约定（RS256）

### 3.1 签名算法

- `alg`：`RS256`
- `kid`：必须有（用于 JWKS 轮换）

### 3.2 Claim 约定

必须包含：

- `sub`：内部用户 ID（数值型，以字符串形式承载，等同于 `users.id`，见 `openspec/decisions/0017-primary-key-strategy.md`）
- `iss`：签发者
- `aud`：受众（建议为本底座 API 的标识）
- `exp`：过期时间（必须校验）
- `iat`：签发时间
- `jti`：JWT ID（用于审计/追踪，可选但推荐）

可选：

- `roles`：角色列表（例如 `ADMIN`）

### 3.3 过期策略（MVP 建议）

- `accessToken` 过期：建议 30 分钟～2 小时（实现阶段配置化）
- MVP 不提供 refresh token：过期后要求重新登录

## 4. 接口契约

### 4.1 登录

- `POST /api/auth/login`

请求（JSON）：

- `username`：必填
- `password`：必填

响应（JSON）：

- `accessToken`：JWT 字符串
- `tokenType`：固定 `Bearer`
- `expiresInSeconds`：可选（便于前端做过期提示）
- `user`：可选
  - `id`（与 `sub` 一致）
  - `username`
  - `roles`

错误码：

- `400`：参数缺失/格式错误
- `401`：用户名或密码错误
- `429`：触发限流（防暴力破解）

### 4.2 JWKS（公钥发布）

- `GET /.well-known/jwks.json`

响应：

- 标准 JWKS JSON（包含当前可用公钥集合，至少包含 `kid/kty/n/e/alg/use` 等字段）

语义约束：

- 资源服务验签应优先走 JWKS（对应 `kid`），以支持平滑轮换

## 5. 安全与合规要求（MVP 必须）

- 密码存储必须使用强哈希（bcrypt/argon2 之一），禁止明文或可逆加密。
- 登录接口必须具备最小防护：
  - 基于 IP/用户名的限流（`429`）
  - 失败次数审计（用于排查与告警）
- JWT 私钥不得出现在仓库中，必须通过外部配置注入。

## 6. 数据模型（建议）

MVP 最小用户表字段建议：

- `id`（MVP 采用 `BIGSERIAL`，见 `openspec/decisions/0017-primary-key-strategy.md`）
- `username`（唯一）
- `password_hash`
- `user_roles`（关联表，见 `openspec/decisions/0018-user-roles-join-table.md`）
- `created_at/updated_at`

## 7. 可观测性与审计（建议）

登录成功/失败需要记录（至少日志，后续可入审计表）：

- `username`
- 成功/失败原因
- `ip`
- `userAgent`
- `jti`（若签发）

## 8. 开放问题（Open Questions）

- `aud/iss` 的具体取值与校验策略（建议在配置中统一约束）。
- 是否需要为平台工具调用提供“短期 token 获取/刷新机制”（后续可用 token exchange 方案 C 解决）。
