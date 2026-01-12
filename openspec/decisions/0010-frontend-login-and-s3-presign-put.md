# 0010：前端管理台采用标准登录 + S3 presigned PUT 直传

## 状态

已决定

## 背景

管理台用于演示与验证底座能力（上传→索引→检索→问答）。为保证闭环“像企业系统”，同时减少额外复杂度，需要对两项关键点定主路径：

1. 登录方式：是否提供标准登录获取 JWT，还是仅提供“粘贴 JWT”。
2. S3 直传方式：presigned PUT 还是 presigned POST。

## 决策

- 管理台采用**标准登录**：通过底座登录接口获取用户 JWT，并按方案A透传（见 `openspec/proposals/0003-platform-auth-and-identity-mapping.md`）。
- S3 直传主路径采用 **presigned PUT**：
  - 底座签发短期有效的 presigned URL
  - 浏览器使用 HTTP PUT 直传对象到 S3
  - 上传完成后回调底座创建文档记录并触发索引

## 影响与权衡

### 标准登录

优点：

- 用户身份链路完整，权限隔离与审计更清晰。
- 与平台接入方案A一致（用户级 JWT）。

代价：

- 需要实现登录接口与最小用户体系（MVP 仍可简化为内置用户/演示账号）。

### presigned PUT

优点：

- 前端实现最直接：拿到 URL 直接 PUT 文件即可。
- 便于在调试阶段快速验证对象存储链路。

代价：

- 需要正确配置 S3 CORS（允许浏览器 PUT 与必要请求头）。
- 对上传约束（如严格字段校验、表单字段约束）弱于 presigned POST（后续可增强）。

## 后续工作

- 在 `openspec/specs/0002-frontend-admin-console-spec.md` 固化登录接口与 presigned PUT 契约。
- 如需要更强约束（文件类型/大小/表单字段），后续可新增 presigned POST 作为可选能力，但不改变主路径语义。

