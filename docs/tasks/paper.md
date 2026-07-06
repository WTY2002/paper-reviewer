# paper 模块现状

## 已实现

- [x] `POST /api/papers` 上传 PDF
- [x] 非空、扩展名、MIME、`%PDF-` 魔数、20 MiB、300 页和用户配额校验
- [x] PDFBox 标题、页数和全文提取
- [x] 保存 `original.pdf`、`extraction.txt`、Paper 与 PaperExtraction 记录
- [x] `GET /api/papers` 当前用户列表
- [x] `GET /api/papers/{paperId}/pdf` 受保护 PDF
- [x] 前端 Dropzone、上传流程和 PDF Viewer
- [x] Controller/Service/上传组件测试

## 当前边界

- [ ] 没有 `GET /api/papers/{paperId}` 详情接口
- [ ] 没有公开的 `DELETE /api/papers/{paperId}`；`PaperService.delete` 仅供内部清理
- [ ] 列表不分页、不筛选
- [ ] 扫描型 PDF 没有 OCR
