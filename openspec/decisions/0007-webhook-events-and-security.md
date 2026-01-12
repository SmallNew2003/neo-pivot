# 0007：Webhook 事件规范与安全策略（供 n8n 等平台订阅）

## 状态

已决定

## 背景

为支持 n8n 等流程编排平台接入，需要核心底座对外提供“可订阅的事件通知”，用于：

- 索引成功/失败后的自动化处理（告警、工单、重试、推送）。
- 跨系统同步与运营动作触发。

同时，Webhook 属于对外暴露能力，必须具备基本安全与幂等能力，避免伪造、重放与重复投递导致的副作用。

## 决策

- 核心底座对外提供 Webhook 事件推送能力（增强项，可在 MVP 后置实现，但规范先确定）。
- Webhook 采用“事件类型 + 幂等 eventId + 签名 + 时间戳”的统一信封（envelope）格式。
- 安全策略采用 HMAC-SHA256 签名 + 时间窗口校验，以防篡改与重放。

## 事件类型（建议最小集合）

1. `document.indexing.succeeded`
2. `document.indexing.failed`

可选扩展（后续按需增加）：

- `document.indexing.started`
- `document.indexing.retrying`
- `document.deleted`

## 事件数据结构（建议）

Webhook 请求体为 JSON，统一 envelope：

- `eventId`：事件唯一 ID（UUID），用于幂等去重
- `eventType`：事件类型（如 `document.indexing.failed`）
- `occurredAt`：ISO-8601 时间戳（事件发生时间）
- `data`：事件负载

`data` 字段建议包含：

- `documentId`
- `ownerId`
- `status`：`INDEXED` / `FAILED` 等
- `errorMessage`：仅失败时提供（可裁剪长度）
- `metrics`：可选（例如耗时、chunk 数量、embedding 数量）

## 投递与重试策略（建议）

- HTTP 方法：`POST`
- 超时：建议 3–10 秒（实现阶段配置化）
- 重试：指数退避（例如 1s/5s/30s/2m/10m），最大重试次数可配置
- 失败处理：超过重试次数后记录死信（dead-letter）并暴露可查询/可手动重放能力（后续增强）

## 幂等与去重（必须）

- 接收方（如 n8n）以 `eventId` 去重；同一 `eventId` 多次到达必须被视为同一事件。
- 发送方在重试时必须复用同一个 `eventId`，不得每次重试生成新 ID。

## 安全与防重放（必须）

### 签名

使用共享密钥 `webhook.secret` 计算 HMAC：

- 签名算法：HMAC-SHA256
- 签名内容：`timestamp + "." + rawBody`

### 请求头（建议）

- `X-Webhook-Timestamp`：毫秒或秒级时间戳（与签名计算一致）
- `X-Webhook-Signature`：签名值（例如 `sha256=<hex>`）
- `X-Webhook-Event-Id`：冗余携带 `eventId`（便于网关/日志检索）
- `X-Webhook-Event-Type`：冗余携带 `eventType`

### 校验规则（接收方）

- 校验签名一致性（防篡改）
- 校验时间窗口（例如 5 分钟）：
  - 若 `abs(now - timestamp) > window` 则拒绝（防重放）
- 对 `eventId` 做持久化去重（防重复投递）

## 与平台的对接方式（建议）

- n8n：使用 Webhook Trigger 接收事件，先做签名校验与去重，再进入业务流程。
- Dify/Coze：通常不直接消费 webhook（除非用于运营/告警），优先走“工具调用”模式。

## 后续工作

- 在 specs 中补充 Webhook 配置项与订阅管理（注册、启停、目的地 URL、密钥轮换）。
- 确定是否需要对事件负载做脱敏与字段白名单（与权限/审计策略一致）。

