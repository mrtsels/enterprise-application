---
name: enterprise-app-reference
description: 专精特新企业申报管理平台的架构、模块、工作流及核心约定说明
metadata: 
  node_type: memory
  type: reference
  originSessionId: 3e1bafa7-920b-451b-acba-341ad47ec06c
---

# Enterprise Application — 专精特新企业申报管理平台

> 聚焦 **提交申报材料** 与 **填写申报项目** 两大核心功能。

## 技术栈

- **后端**: Spring Boot 3.4.5 + Java 17, Maven
- **前端**: Vanilla JavaScript SPA (无框架), 中文UI
- **数据库**: MySQL 8.4 (Docker: 3306, 本地: 3308)
- **迁移**: Flyway (`spring.jpa.hibernate.ddl-auto=update` + `flyway.baseline-on-migrate=true`)
- **端口**: 后端 8080, API 基础路径 `/api/`

## 项目结构

```
enterprise-application/
├── backend/src/main/java/com/gz/enterprise/application/
│   ├── config/        # SecurityConfig, CorsConfig
│   ├── controller/    # REST 控制器
│   ├── domain/        # JPA 实体 (继承 BaseEntity)
│   ├── exception/     # GlobalExceptionHandler
│   ├── repository/    # Spring Data JPA Repositories
│   └── service/       # 业务逻辑层
├── index.html         # 主 SPA 前端
└── docker-compose.yml # MySQL 8.4 + 后端
```

## 分层模式

所有模块遵循 `Controller → Service → Repository` 结构，均使用 `@RequiredArgsConstructor` 构造注入。

## 核心领域模块

| 资源 | 路径 | 领域类 | 说明 |
|------|------|--------|------|
| 申报项目 | `/api/declarations` | `DeclarationApplication` | 专精特新申报项目 CRUD |
| 申报材料 | `/api/materials` | `DeclarationMaterial` | 材料上传/管理 |
| 文档 | `/api/documents` | `Document` | 文件管理 |
| AI评估 | `/api/declarations/{id}/ai-evaluate` | — | AI预审评分 |
| 仪表盘 | `/api/dashboard` | — | 统计数据 |

## 申报工作流

```
草稿(DRAFT) → 提交(SUBMITTED) → 审核中(PENDING) → 通过(APPROVED) / 驳回(REJECTED)
```

## AI 评分维度 (专精特新认定标准)

- **专业化指标** (25分) — 市场年限、营收占比、增长率、产品领域
- **精细化指标** (25分) — 数字化水平、质量管理、资产负债率、净利润率
- **特色化指标** (15分) — 重点领域、特色技术、人才项目
- **创新能力指标** (35分) — 知识产权、研发投入、研发人员占比

## 申报材料分类

| 分组 | 材料示例 | 必交 |
|------|----------|------|
| 基础资质 | 营业执照、中小企业划型认定证明、企业章程、法定代表人身份证明 | 是 |
| 财务指标 | 近两年财务审计报告、近三年研发费用专项审计报告、主营业务收入明细表 | 是 |
| 创新能力 | 知识产权证书、研发机构证明、研发人员花名册、科技成果转化说明 | 部分必交 |
| 经营规范 | ISO9001认证、信息化系统说明、无违法记录承诺函、安全环保证明 | 部分必交 |
| 特色化 | 自有品牌证明、市场占有率报告、细分市场排名证明 | 部分必交 |

## API 风格

- RESTful, 分页列表 `?page=0&size=20`, 筛选通过 query params
- 错误处理: `IllegalArgumentException` → 400, 其他 `Exception` → 500

## 常见任务

- **添加新实体**: 创建 Domain 类 (继承 BaseEntity) → 创建 JpaRepository → 创建 Service → 创建 Controller → 添加 Flyway 迁移
- **运行迁移测试**: 启动 Docker MySQL 后执行 `mvn flyway:migrate`
