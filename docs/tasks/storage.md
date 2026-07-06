# storage 模块现状

## 已实现

- [x] `storage/tmp` 随机临时上传目录
- [x] `storage/papers/{userId}/{paperId}` Paper 文件目录
- [x] `storage/exports/{userId}/{reviewId}` 导出目录
- [x] 临时文件移动、文本/导出写入、受保护读取和递归物理删除
- [x] 500 MiB 默认用户 Paper 存储配额
- [x] 绝对路径规范化、受管根目录检查、文件名检查和符号链接防护
- [x] 存储和配额测试

## 当前边界

- [ ] 只支持本地磁盘，没有对象存储适配
- [ ] 配额只统计 Paper 文件，不单独统计导出文件
- [ ] 文件删除失败只记录日志并由部分上层流程映射错误
