# OCR 营业执照识别

## 概述

本项目采用 **AI 视觉大模型**（兼容 OpenAI 协议的视觉模型，如 DeepSeek-VL / GPT-4o-vision）代替传统 OCR 引擎，识别上传的营业执照图片，将提取的结构化字段自动填充到申报表单中。目前仅针对 **营业执照（副本）** 一种材料类型实现。

## 架构流程

```
用户上传图片
    │
    ▼
[前端] form/index.html
    │  上传 → POST /api/declarations/{id}/ocr/business-license
    ▼
[后端] DeclarationController
    │
    ▼
[后端] DeclarationService.recognizeBusinessLicense()
    │  查找当前申报的"营业执照（副本）"材料记录
    ▼
[后端] OcrService.recognize()
    │  1. 保存文件 → FileStorageService → Document 记录
    │  2. Base64 编码图片
    │  3. 构造 OpenAI-compatible 请求体 → 调用视觉 API
    │  4. 解析响应 JSON
    │  5. 持久化识别结果 → DeclarationMaterial.identifyResult
    │  6. 更新材料状态 → IDENTIFIED
    ▼
[前端] 接收返回的字段 Map → 填充表单（addOcrBadge 标记+OCR✓标记）
```

## 后端

### OcrService (核心)

**文件**: [OcrService.java](../backend/src/main/java/com/gz/enterprise/application/service/OcrService.java)

- `recognize(file, declarationId, materialId, enterpriseId, materialName)` — 主入口
- 校验文件非空、不超过 10MB
- 调用 `FileStorageService.store()` 保存文件并创建 `Document` 记录
- 将图片编码为 Base64 Data URL
- 构造 OpenAI Chat Completions 格式请求（含 system prompt + user image）
- 调用视觉 API（端点 `/v1/chat/completions`）
- 解析返回的 JSON（支持剥离 markdown 代码块包裹）
- 将识别结果 JSON 写入 `DeclarationMaterial.identifyResult`，状态更新为 `IDENTIFIED`
- 结果以 `Map<String, Object>` 返回给调用方

### 请求体格式

```json
{
  "model": "配置的模型名",
  "max_tokens": 81920,
  "reasoning": false,
  "messages": [
    {
      "role": "system",
      "content": "你是一个专业的中国营业执照OCR识别系统..."
    },
    {
      "role": "user",
      "content": [
        { "type": "text", "text": "请提取这张中国营业执照图片中的所有字段，仅返回JSON。" },
        { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,..." } }
      ]
    }
  ]
}
```

### DeclarationService

**文件**: [DeclarationService.java](../backend/src/main/java/com/gz/enterprise/application/service/DeclarationService.java)

- `recognizeBusinessLicense(file, declarationId)` — 查找当前申报的所有 `DeclarationMaterial`，过滤出 `materialName == "营业执照（副本）"` 的记录，代理调用 `OcrService`
- 未找到材料时抛出 `IllegalArgumentException("未找到营业执照材料记录，请先创建申报")`

### DeclarationController

**文件**: [DeclarationController.java](../backend/src/main/java/com/gz/enterprise/application/controller/DeclarationController.java) (第 118-123 行)

```
POST /api/declarations/{id}/ocr/business-license
Content-Type: multipart/form-data
参数: file (MultipartFile)

返回: Map<String, Object> — 识别的字段键值对
```

## 提取字段

AI 返回的 JSON 对象包含以下字段（由 system prompt 定义）：

| 字段名 | 说明 | 映射表单字段 |
|--------|------|-------------|
| `companyName` | 企业名称 | `f_name` |
| `creditCode` | 统一社会信用代码 | `f_creditCode` |
| `legalRepresentative` | 法定代表人 | `f_legalRep` |
| `registeredCapital` | 注册资本（中文） | — |
| `capitalAmount` | 注册资本（纯数字） | `f_regCapital` |
| `capitalUnit` | 注册资本单位（如"万元"） | — |
| `establishedDate` | 成立日期（YYYY-MM-DD） | `f_estDate` |
| `fullAddress` | 完整地址 | `f_address` |
| `province` | 省 | 省下拉框 |
| `city` | 市 | 市下拉框 |
| `district` | 区 | 区下拉框 |
| `businessScope` | 经营范围 | — |
| `registrationAuthority` | 登记机关 | — |
| `validPeriod` | 营业期限 | — |

缺失或不可见的字段返回 `null`。

## 前端

**文件**: [frontend/form/index.html](../frontend/form/index.html) (约第 616-898 行)

### 上传界面
- 申报表单顶部渲染营业执照上传卡片，含拖拽/点击 dropzone
- 文件类型限制：JPG/PNG
- 大小限制：10MB
- 图片预览

### 识别请求
- 使用 `XMLHttpRequest` 上传，带进度条
- URL: `/api/declarations/{id}/ocr/business-license`
- 超时 60 秒
- 按钮文字区分"上传"和"OCR识别"两种模式

### 表单填充
- `applyBlOcrResult(data)` 将 OCR 返回字段映射到表单各 input
- 每个被 OCR 填充的字段旁追加绿色 `✓ OCR` 徽标（`addOcrBadge()`）

## 配置

### application.yml

```yaml
ai:
  vision:
    api-key: ${AI_VISION_KEY:sk-placeholder}
    base-url: ${AI_VISION_URL:http://localhost:8000}
    model: ${AI_VISION_MODEL:gpt-4o-vision}
    max-tokens: 81920
```

### AiConfigStore

**文件**: [AiConfigStore.java](../backend/src/main/java/com/gz/enterprise/application/config/AiConfigStore.java)

- 运行时配置存储，持久化到 `{uploadPath}/ai-config.json`
- 支持运行时通过 API 更新 baseUrl / model / apiKey / prompts
- 默认 system prompt 针对 **营业执照（副本）** 优化，提示模型以严格 JSON 格式返回指定字段
- `getPrompt(materialName)` 按材料名查找对应提示词，支持扩展其他材料类型

### DeepSeekProperties

**文件**: [DeepSeekProperties.java](../backend/src/main/java/com/gz/enterprise/application/config/DeepSeekProperties.java)

- `@ConfigurationProperties(prefix = "ai.vision")` 映射配置属性

## 提示词（Prompt）系统

当前仅定义了一个提示词，针对 `营业执照（副本）`：

> 你是一个专业的中国营业执照OCR识别系统。从图片中提取字段并以严格的JSON格式返回。只输出一个纯JSON对象，不要任何markdown、代码块标记或额外文字。JSON字段名必须使用特定驼峰命名... establishedDate格式为YYYY-MM-DD。capitalAmount是纯数字，capitalUnit是人民币单位。province/city/district从fullAddress中解析提取。

系统设计支持按材料名扩展多个提示词，`AiConfigStore.getPrompt()` 已预留此能力。

## 材料状态流转

```
MISSING → UPLOADED → IDENTIFIED → VERIFIED
                     ↑
                   OCR 完成
```

- OCR 识别完毕后材料状态更新为 `IDENTIFIED`
- 识别结果 JSON 存入 `identifyResult` 字段

## 错误处理

| 场景 | HTTP 状态码 | 错误信息 |
|------|-------------|---------|
| 文件为空 | 400 | 文件为空 |
| 文件超 10MB | 400 | 文件大小超过10MB限制 |
| API 不可用 | 500 | 营业执照识别服务暂时不可用，请稍后重试 |
| 响应格式异常 | 400 | OCR响应异常：缺少choices / 内容为空 |
| JSON 解析失败 | 400 | OCR识别结果解析失败，请重试 |
| 未找到材料 | 400 | 未找到营业执照材料记录，请先创建申报 |
| 文件类型不支持 | 400 | FileStorageService 校验 |
