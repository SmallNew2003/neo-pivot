# 0001：认证选择 JWT RS256（含 JWKS 轮换）

## 状态

已决定

## 背景

MVP 需要一个标准、可扩展且便于团队落地与演进的认证方案，同时满足后续：

- 认证与业务服务解耦（可由独立身份提供方签发 Token）。
- 密钥可轮换（不因换密钥而全量停机）。
- 与 Spring Security 生态对齐。

## 决策

- 采用 JWT Bearer Token。
- 签名算法使用 `RS256`（非对称）。
- 验签公钥推荐通过 JWKS 发布（支持 `kid` 轮换）；MVP 允许先以配置注入单把公钥进行简化。

## 具体约定

- 客户端请求头：`Authorization: Bearer <JWT>`
- 必须校验：
  - `exp`（过期时间）
  - `iss`（签发者）
  - `aud`（受众）
- 最小 claim：
  - `sub`：用户唯一标识
  - `roles`：可选角色列表（例如包含 `ADMIN`）
- 建议：
  - 使用 `kid` + JWKS 支持密钥轮换

## 影响与权衡

优点：

- 更企业化：符合常见 OAuth2 Resource Server 的验签方式。
- 更安全：私钥不需要下发到资源服务（只需公钥）。
- 可演进：未来可直接接入企业 IdP（Keycloak、Auth0、Azure AD 等）。

代价：

- 配置项更多（issuer/audience/jwksUri 或 publicKey）。
- 本地开发需要生成并管理一套 keypair（或使用 dev 签发工具）。

## 后续工作

- 明确 MVP 的“签发端”形态：
  - 选项 A：仅做资源服务（不提供登录），token 由外部脚本/工具生成。
  - 选项 B：提供 dev-only 的 token 签发端点（仅本地，用于演示）。
