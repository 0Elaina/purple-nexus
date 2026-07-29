# Purple Nexus 开发指南

## 1. 文档职责

本文档定义 Purple Nexus 的开发环境、本地启动、环境变量、Git 协作、质量检查和数据迁移规则。

本文档不重复业务范围、系统架构、API 字段、数据库字段或具体框架设计。

## 2. 开发环境

| 工具 | 版本基线 | 说明 |
| --- | --- | --- |
| JDK | 21 LTS | Spring Boot 运行时 |
| Spring Boot | 3.5.x | 补丁版本由 `pom.xml` 锁定 |
| Maven | Maven Wrapper | 不依赖全局 Maven 版本 |
| Node.js | 24 LTS | 前端运行时 |
| pnpm | 11.x | 前端依赖管理 |
| Python | 3.13.x | AI 服务运行时 |
| uv | 项目锁定版本 | Python 环境与依赖管理 |
| Docker Compose | v2 | 本地基础设施 |
| Git | 2.x | 版本控制 |

依赖版本只在各自的构建文件和锁文件中维护：

- Java：`pom.xml` 和 Maven Wrapper。
- Web：`package.json` 和 `pnpm-lock.yaml`。
- Python：`pyproject.toml` 和 `uv.lock`。
- 基础设施：`compose.yaml`。

## 3. 仓库目录约定

```text
purple-nexus/
├── apps/
│   ├── web/
│   ├── server/
│   └── agent/
├── infra/
├── docs/
└── compose.yaml
```

目录使用简短名称，应用标识分别为 `purple-nexus-web`、`purple-nexus-server` 和 `purple-nexus-agent`。

## 4. 本地启动

首次启动前，根据根级和各应用的 `.env.example` 创建本地 `.env`。基础设施配置和 Compose 命令位于 Ubuntu VM 的仓库检出中；Web、Server 和 Agent 配置及启动命令位于 Windows 仓库检出中，并分别在独立 PowerShell 终端执行。两端仓库应指向同一提交。

### 4.1 基础设施

```bash
docker compose up -d
```

### 4.2 AI 服务

```powershell
Set-Location apps/agent
uv sync --locked
uv run uvicorn purple_nexus_agent.main:app --reload
```

### 4.3 Spring Boot 服务

```powershell
Set-Location apps/server
.\mvnw.cmd spring-boot:run
```

### 4.4 Web 应用

```powershell
Set-Location apps/web
pnpm install --frozen-lockfile
pnpm dev
```

在 Ubuntu VM 的仓库根目录关闭本地基础设施：

```bash
docker compose down
```

`docker compose down` 不删除持久化数据；只有在明确需要重建本地数据时才删除数据卷。

## 5. 环境变量

| 服务 | 必需变量 |
| --- | --- |
| Docker Compose | `POSTGRES_DB`、`POSTGRES_USER`、`POSTGRES_PASSWORD`、`REDIS_PASSWORD`、`QDRANT_API_KEY`、`RUSTFS_ACCESS_KEY`、`RUSTFS_SECRET_KEY`、`RUSTFS_BUCKET` |
| Web | `VITE_API_BASE_URL` |
| Spring Boot | `SPRING_PROFILES_ACTIVE`、`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_HOST`、`SPRING_DATA_REDIS_PORT`、`SPRING_DATA_REDIS_PASSWORD` |
| Spring Boot → FastAPI | `AGENT_BASE_URL`、`AGENT_SERVICE_TOKEN` |
| 对象存储 | `S3_ENDPOINT`、`S3_ACCESS_KEY`、`S3_SECRET_KEY`、`S3_BUCKET`、`S3_REGION`、`S3_PATH_STYLE_ACCESS` |
| FastAPI | `APP_ENV`、`QDRANT_URL`、`QDRANT_API_KEY`、`REDIS_URL`、`AGENT_SERVICE_TOKEN` |
| 模型服务 | `LLM_API_KEY`、`LLM_BASE_URL`、`LLM_MODEL`、`EMBEDDING_MODEL` |

- 每个应用维护自己的 `.env.example`，只提供安全的示例值。
- 本地 `.env` 和真实密钥禁止提交到仓库。
- `VITE_` 开头的变量会进入浏览器产物，禁止存放任何密钥。
- 新增或删除环境变量时，必须同步更新对应的 `.env.example`。

## 6. Git 协作规范

### 6.1 分支

| 分支 | 用途 |
| --- | --- |
| `main` | 始终保持可发布 |
| `develop` | 日常集成 |
| `feature/<module>` | 功能开发 |
| `fix/<issue>` | 缺陷修复 |
| `refactor/<module>` | 重构 |
| `docs/<topic>` | 文档调整 |

功能分支默认从 `develop` 创建，并通过合并请求回到 `develop`：

```powershell
git switch develop
git pull --ff-only
git switch -c feature/project-list

# 完成开发和检查后
git add .
git commit -m "feat(server): add project list"
git push -u origin feature/project-list
```

### 6.2 提交消息

提交消息采用 Conventional Commits：

```text
<type>(<scope>): <subject>
```

| Type | 用途 |
| --- | --- |
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `refactor` | 不改变行为的重构 |
| `docs` | 文档 |
| `test` | 测试 |
| `build` | 构建或依赖 |
| `ci` | CI 配置 |
| `chore` | 其他维护 |

Scope 使用 `web`、`server`、`agent`、`infra` 或 `docs`。Subject 使用简洁的英文祈使句，不加句号。

## 7. 质量检查

合并前必须通过对应应用的检查：

| 应用 | 命令 |
| --- | --- |
| Web | `pnpm lint`、`pnpm test`、`pnpm build` |
| Spring Boot | `.\mvnw.cmd verify` |
| FastAPI | `uv run ruff check .`、`uv run ruff format --check .`、`uv run pytest` |
| 基础设施 | `docker compose config` |

格式化、静态检查和测试规则由各应用配置维护，不在本文档中重复。

## 8. 数据迁移

- PostgreSQL 业务结构只通过 Spring Boot 中的 Flyway 迁移修改。
- 迁移文件采用 `V<序号>__<英文描述>.sql`，一份迁移只处理一个逻辑变更。
- 已执行的迁移文件禁止修改；修复或回退通过新增迁移完成。
- 测试数据不得写入生产迁移。
- Qdrant Collection 结构由 AI 服务中的版本化初始化代码管理，禁止仅在控制台手工修改。
- 向量索引属于派生数据；结构不兼容时，创建新 Collection 并从已发布博客重新构建。
