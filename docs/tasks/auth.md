# auth 模块现状

## 已实现

- [x] 邮箱、密码、显示名注册；密码 BCrypt 哈希
- [x] 邮箱密码登录并签发 JWT
- [x] `GET /api/auth/me`
- [x] 除 register/login 外的 `/api/**` 鉴权
- [x] 无 Session 的 Spring Security 配置和 JSON 401 响应
- [x] 前端 Token 保存、刷新恢复、登录/注册页和路由保护
- [x] 注册、登录、无效 Token 集成测试和 authStore 测试

## 当前边界

- [ ] 没有服务端 `POST /api/auth/logout`；退出只清理前端 Token
- [ ] 没有刷新 Token、吊销列表、邮箱验证或密码找回
