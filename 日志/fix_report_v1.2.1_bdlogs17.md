# C2CMarket 部署故障修复报告

## 修复尝试 #18 - bdlogs17.csv 部署失败

**日期**: 2026-04-27
**版本**: v1.2.1
**提交**: b9aa1db
**日志来源**: bdlogs17.csv

---

## 1. 故障现象

### 构建日志关键错误

```
added 55 packages, and audited 56 packages in 7s    ← 仅安装55个包（应为102+）

✓ 3 modules transformed.
x Build failed in 53ms

error during build:
[vite:build-import-analysis] src/App.vue (11:21):
Failed to parse source for import analysis because the content contains
invalid JS syntax. Install @vitejs/plugin-vue to handle .vue files.
```

### 错误定位

| 指标 | 实际值 | 期望值 | 偏差 |
|------|--------|--------|------|
| npm 安装包数量 | 55 | 102+ | -46% |
| Vite 构建状态 | 失败 (53ms) | 成功 | N/A |
| @vitejs/plugin-vue | 未安装 | 已安装 | 缺失 |
| Vite 转换模块数 | 3 | 15+ | -80% |

---

## 2. 根因分析

### 直接原因

`@vitejs/plugin-vue`（devDependency）未被 npm 安装，导致 Vite 无法解析 `.vue` 文件。

### 根本原因

**npm 在 Docker 构建环境中跳过了 devDependencies 安装**。

原因链条：

1. **Railway 平台行为**：Railway 在 Docker 构建阶段注入服务环境变量。若用户在 Railway 控制台设置了 `NODE_ENV=production`，该变量会在构建时生效。

2. **npm 的 NODE_ENV 敏感行为**：
   - 当 `NODE_ENV=production` 时，`npm install` 自动跳过 devDependencies
   - `@vitejs/plugin-vue` 和 `vite` 均声明为 devDependencies
   - 结果：仅安装 55 个生产依赖包，缺少关键的构建工具

3. **Dockerfile 未显式设置 NODE_ENV**：原 Dockerfile 没有在 npm install 前设置 `NODE_ENV=development`，导致 Railway 注入的环境变量覆盖了默认行为。

### 佐证

- 本地 `npm run build` 完全正常（本地 NODE_ENV 未设置为 production）
- 本地 `npm ls --depth=0` 显示全部 5 个直接依赖均已安装
- package-lock.json 包含 102 个包条目，但 Docker 中仅安装 55 个
- 55 个包恰好约等于仅安装 production dependencies 的数量

### 排除的其他假设

| 假设 | 排除理由 |
|------|----------|
| package-lock.json 平台兼容性问题 | 日志显示使用的是 `npm install`（非 `npm ci`），且未复制 lockfile |
| npm registry 网络问题 | 55 个包成功下载，网络通畅 |
| Docker 构建上下文缺失文件 | COPY 命令执行成功，package.json 正确复制 |
| .npmrc 配置干扰 | 项目中不存在 .npmrc 文件 |
| node:20-alpine 镜像问题 | 官方镜像不预设 NODE_ENV |

---

## 3. 修复方案

### 修改文件

**Dockerfile** (v1.2.0 → v1.2.1)

### 关键变更

```dockerfile
# ===== 修改前 (v1.2.0) =====
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

# ===== 修改后 (v1.2.1) =====
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
ENV NODE_ENV=development                          # 新增：强制开发模式
COPY frontend/package.json ./                     # 修改：不复制 package-lock.json
RUN npm install --include=dev 2>&1 && \           # 修改：npm install 替代 npm ci
    echo "=== NODE_ENV: $NODE_ENV ===" && \       # 新增：诊断输出
    npm ls --depth=0 2>&1 && \                    # 新增：列出直接依赖
    find node_modules -maxdepth 2 -name "package.json" | wc -l && \
    test -f node_modules/@vitejs/plugin-vue/package.json && \
      echo "OK: @vitejs/plugin-vue found" || \
      echo "ERROR: @vitejs/plugin-vue NOT FOUND"
```

### 变更说明

| 变更项 | 原值 | 新值 | 原因 |
|--------|------|------|------|
| NODE_ENV | 未设置（继承环境） | development | 强制 npm 安装 devDependencies |
| 包管理命令 | npm ci | npm install --include=dev | 避免锁文件兼容问题，显式包含 dev |
| 复制文件 | package.json + package-lock.json | 仅 package.json | 避免 macOS 生成锁文件在 Linux 的兼容问题 |
| 诊断输出 | 无 | npm ls + 包计数 + 关键包验证 | 下次构建可快速定位问题 |

---

## 4. 验证计划

### 构建验证检查点

1. **npm install 输出**：确认安装包数量 > 100
2. **诊断输出**：确认 `OK: @vitejs/plugin-vue found`
3. **Vite 构建**：确认 `✓ built in XXXms` 无错误
4. **dist 目录**：确认生成 index.html + JS/CSS 资源文件
5. **Spring Boot 启动**：确认 JAR 包含前端静态资源

### 运行时验证检查点

1. **HTTP 200**：访问 `https://c2cmarket.store` 返回前端页面
2. **API 响应**：`/api/v2/categories/tree` 返回 JSON 数据
3. **无 502 错误**：所有 API 端点正常响应

---

## 5. 风险评估

| 风险项 | 等级 | 缓解措施 |
|--------|------|----------|
| npm install 比 npm ci 慢 | 低 | 仅增加约 10-20 秒构建时间 |
| 无锁文件导致依赖版本漂移 | 中 | 构建时生成新锁文件，Vite 5.x 稳定 |
| NODE_ENV=development 影响运行时 | 无 | 仅在 frontend-build 阶段设置，不影响 Stage 3 运行时 |
| 诊断输出增加构建日志量 | 低 | 输出精简，便于排查 |

---

## 6. 历史修复记录

| # | 日志文件 | 问题 | 修复 | 版本 |
|---|----------|------|------|------|
| 1 | bdlogs4.json | Maven 基础镜像不存在 | 改用 eclipse-temurin + 手动安装 Maven | v1.1.0 |
| 2 | bdlogs5.json | Maven 下载 404/gzip 格式错误 | 切换 archive.apache.org 备用源 | v1.1.1 |
| 3 | dplogs4.json | MYSQLHOST 环境变量未设置 | 三级回退：DB_* → MYSQL* → 默认值 | v1.1.1 |
| 4 | dplogs6.json | ALTER TABLE IF NOT EXISTS 语法错误 | 改用 CREATE TABLE IF NOT EXISTS | v1.1.1 |
| 5 | dplogs6.json | UNIQUE INDEX LEAST/GREATEST 不支持 | 移除函数式唯一索引 | v1.1.1 |
| 6 | dplogs8.json | RedisTemplate Bean 创建失败 | @ConditionalOnProperty 条件注入 | v1.1.2 |
| 7 | dplogs9.json | MyBatis #{} 不支持三元表达式 | 改用 \<script\>\<if\> 动态 SQL | v1.1.2 |
| 8 | bdlogs12.csv | Maven 3.9.9 下载 404 | 升级到 3.9.15 + 备用 URL | v1.1.3 |
| 9 | bdlogs13.csv | @vitejs/plugin-vue 未安装 | 移除 --prefer-offline 标志 | - |
| 10 | bdlogs14.csv | Docker 层缓存使用旧代码 | CACHE_BUST 合并到首个 RUN | - |
| 11 | bdlogs15.csv | npm ci 仅安装 56 包 | 改用 3 阶段构建 | - |
| 12 | bdlogs16.csv | lockfileVersion 3 平台兼容 | npm install 替代 npm ci | - |
| 13 | bdlogs17.csv | npm install 仅安装 55 包 | NODE_ENV=development + 诊断输出 | v1.2.1 |

---

## 7. 下一步

- [ ] 等待 Railway 构建完成，获取新构建日志
- [ ] 验证构建日志中诊断输出（包数量、@vitejs/plugin-vue 状态）
- [ ] 验证部署后应用正常运行
- [ ] 若构建仍失败，根据诊断输出进一步调整
