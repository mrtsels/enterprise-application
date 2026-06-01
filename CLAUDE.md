# CLAUDE.md — Enterprise Application

本项目是专精特新企业申报管理平台，聚焦 **提交申报材料** 与 **填写申报项目** 两大核心功能。基于 Spring Boot 3 + Java 17 构建。

---

## Build & Run

```bash
# 全栈启动 (Docker)
docker compose up --build

# 后端单独启动 (本地)
cd backend && mvn clean package -DskipTests
$JAVA_HOME/bin/java -jar target/enterprise-application-0.1.0-SNAPSHOT.jar

# 前端 (静态文件服务器)
python3 -m http.server 3000
```

- **Java**: 17, Maven
- **后端端口**: 8080
- **MySQL**: 3306 (Docker), 3308 (本地)
- **Flyway**: 启动时自动迁移 (`spring.jpa.hibernate.ddl-auto=update` + `flyway.baseline-on-migrate=true`)

---

## 项目结构

```
enterprise-application/
├── backend/                              # Spring Boot 3.4.5 + Java 17
│   └── src/main/java/com/gz/enterprise/application/
│       ├── config/                       # SecurityConfig, CorsConfig
│       ├── controller/                   # REST 控制器
│       ├── domain/                       # JPA 实体 (继承 BaseEntity)
│       ├── exception/                    # GlobalExceptionHandler
│       ├── repository/                   # Spring Data JPA Repositories
│       └── service/                      # 业务逻辑层
├── index.html                            # 主 SPA 前端
├── docker-compose.yml                    # MySQL 8.4 + 后端
└── CLAUDE.md                             # 本文件
```

## 架构

### 分层模式 (Controller → Service → Repository)

所有模块遵循统一结构:
- **Controller**: REST端点 (`@RestController`, `@RequestMapping("/api/<resource>")`)
- **Service**: 业务逻辑、校验、AI评分
- **Repository**: `JpaRepository<Entity, Long>` + 自定义查询
- **Domain**: JPA `@Entity` 继承 `BaseEntity`

所有 Controller 使用 `@RequiredArgsConstructor` 构造注入。Service 也使用 `@RequiredArgsConstructor`。

### 核心领域模块

| 资源 | 路径 | 领域 | 说明 |
|------|------|------|------|
| 申报项目 | `/api/declarations` | `DeclarationApplication` | 专精特新申报项目 CRUD |
| 申报材料 | `/api/materials` | `DeclarationMaterial` | 材料上传/管理 |
| 文档 | `/api/documents` | `Document` | 文件管理 |
| AI评估 | `/api/declarations/{id}/ai-evaluate` | — | AI预审评分 |
| 仪表盘 | `/api/dashboard` | — | 统计数据 |

### 申报工作流

```
草稿(DRAFT) → 提交(SUBMITTED) → 审核中(PENDING) → 通过(APPROVED) / 驳回(REJECTED)
```

### AI 评分维度 (专精特新认定标准)

- **专业化指标** (25分) — 市场年限、营收占比、增长率、产品领域
- **精细化指标** (25分) — 数字化水平、质量管理、资产负债率、净利润率
- **特色化指标** (15分) — 重点领域、特色技术、人才项目
- **创新能力指标** (35分) — 知识产权、研发投入、研发人员占比

### 前端

- Vanilla JavaScript SPA (无框架)
- 中文UI，企业端申报页面
- API基础路径: `/api/`

### 数据库

- MySQL 8.4 (Docker配置在 `docker-compose.yml`)
- Flyway迁移: `backend/src/main/resources/db/migration/V*.sql`
- `ddl-auto: update` 同步 JPA 实体

---

## 常见任务

- **添加新实体**: 创建 Domain 类 (继承 BaseEntity) → 创建 JpaRepository → 创建 Service → 创建 Controller → 添加 Flyway 迁移
- **运行迁移测试**: 启动 Docker MySQL 后执行 `mvn flyway:migrate`
- **API风格**: RESTful, 分页列表 `?page=0&size=20`, 筛选通过 query params
- **错误处理**: `IllegalArgumentException` → 400, 其他 `Exception` → 500

## 申报材料分类

| 分组 | 材料示例 | 必交 |
|------|----------|------|
| 基础资质 | 营业执照、中小企业划型认定证明、企业章程、法定代表人身份证明 | 是 |
| 财务指标 | 近两年财务审计报告、近三年研发费用专项审计报告、主营业务收入明细表 | 是 |
| 创新能力 | 知识产权证书、研发机构证明、研发人员花名册、科技成果转化说明 | 部分必交 |
| 经营规范 | ISO9001认证、信息化系统说明、无违法记录承诺函、安全环保证明 | 部分必交 |
| 特色化 | 自有品牌证明、市场占有率报告、细分市场排名证明 | 部分必交 |

---

## Git 提交规范

**每完成一步、每个细小的更改都必须执行 `git commit`。** 不允许累积多个更改一次性提交。

- **原子粒度**: 一个逻辑更改（如加一个字段、改一处样式、修一个方法）→ 一个 commit
- **提交时机**: 修改完 → 立刻 commit，不允许"攒着一起交"
- **提交信息**: 简明中文描述做了什么，如 `feat: 添加企业注册地字段`、`fix: 修复资产负债率计算溢出`
- **允许空提交**: 如果一个 commit 只有少量变更也不需合并，保证每一步都有记录
- **提交前检查**: 不要提交未完成的半成品（WIP），除非是临时存档需要写明 `wip:` 前缀
- **编辑器自动生成文件**: `.gitignore` 已忽略的无需提交，否则需要确认是否有新增文件需要纳入版本
