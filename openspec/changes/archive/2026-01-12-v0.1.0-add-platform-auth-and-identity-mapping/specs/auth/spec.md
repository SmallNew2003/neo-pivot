## ADDED Requirements

### Requirement: 平台接入的用户身份可追溯
系统 SHALL 在平台接入场景下稳定识别终端用户身份，并据此执行权限过滤与审计记录。

#### Scenario: 透传用户 JWT 的请求鉴权
- **WHEN** 平台调用核心底座接口并携带终端用户 JWT（`Authorization: Bearer <user_jwt>`）
- **THEN** 核心底座以 JWT `sub` 识别用户并按用户身份进行权限过滤与审计

