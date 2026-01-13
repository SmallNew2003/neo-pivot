## ADDED Requirements

### Requirement: API 响应 Envelope 统一契约
系统 SHALL 对业务接口（`/api/**`）的成功与失败响应统一返回 Envelope 结构，同时保留 HTTP 状态码语义。

#### Scenario: 业务异常返回统一结构
- **WHEN** 调用 `/api/**` 发生业务异常或参数校验失败
- **THEN** 响应体按统一 Envelope 返回稳定的 `code/message` 与可选 `details`

