# common-infra 模块现状

## 已实现

- [x] `ApiResponse<T>`：`success/data/error` 统一 JSON 结构
- [x] `BusinessException`、`ErrorCode`、`GlobalExceptionHandler`
- [x] Bean Validation、上传超限和未知异常映射
- [x] JWT、上传限制、时钟及 Web/CORS 配置
- [x] MyBatis 通用 Repository、审计字段基类和 JSON TypeHandler
- [x] 通用响应及异常测试

## 当前边界

- [ ] 没有通用 `PageResult<T>`；当前列表接口返回数组
- [ ] 没有统一 `StorageProperties`/`AiModelProperties`，部分存储和模型配置使用 `@Value`
- [ ] 没有 `deleted_at` 逻辑删除约定
