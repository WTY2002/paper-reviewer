# settings 模块现状

## 已实现

- [x] Settings GET/PUT API
- [x] 返回邮箱、显示名、默认输出语言和当前模型名
- [x] 更新显示名（1～100 字符）
- [x] 更新 `AUTO`、`zh`、`en` 默认语言
- [x] 前端设置页面和保存状态
- [x] 不返回、不展示 API Key 输入
- [x] Settings 服务和页面测试

## 当前边界

- [ ] 响应只返回模型名，不返回 Provider 健康状态或 OpenAI 适配状态
- [ ] 默认语言尚未自动用于 UploadView/ReReviewUploadForm，请求仍默认 `AUTO`
- [ ] 用户不能选择模型或 Provider
