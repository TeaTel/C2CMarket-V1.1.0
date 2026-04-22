# Vercel 部署指南 - 解决 vue-cli-service 错误

## 问题描述
在Vercel部署时出现以下错误：

### 错误1：vue-cli-service 未找到
```
sh: line 1: vue-cli-service: command not found
Error: Command "vue-cli-service build" exited with 127
```

### 错误2：vite 未找到（修复后出现的新错误）
```
sh: line 1: vite: command not found
Error: Command "vite build" exited with 127
```

## 问题原因

### 错误1原因：vue-cli-service 未找到
Vercel错误地将您的Vite项目识别为Vue CLI项目。您的项目使用Vite作为构建工具，但Vercel可能由于以下原因错误识别：
1. Vercel自动检测算法误判
2. 构建缓存问题
3. 配置文件不一致

### 错误2原因：vite 未找到
在修复错误1后，Vercel正确识别了Vite项目，但出现了新的问题：
1. **依赖未正确安装**：Vercel环境中vite命令不可用
2. **安装命令问题**：使用`npm install`而不是`npm ci`，导致依赖不一致
3. **环境配置问题**：Node版本或环境变量配置不正确

## 已验证的配置
✅ 项目已正确配置为Vite项目：
- `package.json`: 使用 `vite build` 命令，添加了 `engines` 字段指定Node版本
- `vite.config.js`: 存在且配置正确
- `vercel.json`: 框架设置为 `"vite"`，安装命令改为 `npm ci --prefer-offline`
- `vercel.toml`: 已添加 `[framework] name = "vite"`
- `package-lock.json`: 存在且版本兼容npm ci
- 本地环境检查：所有依赖正确安装，vite命令可用

## 解决方案

### 步骤1：清除Vercel构建缓存
1. 登录 [Vercel控制台](https://vercel.com)
2. 进入您的项目
3. 点击 **Settings** → **Build & Development Settings**
4. 找到 **"Clear Build Cache"** 选项
5. 点击清除所有构建缓存

### 步骤2：验证项目设置
在Vercel项目设置中确认以下配置：

**Build & Development Settings:**
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Development Command**: `npm run dev`
- **Install Command**: `npm ci --prefer-offline` (重要：使用npm ci确保依赖一致性)

**Environment Variables:**
确保没有设置以下可能干扰构建的环境变量：
- `VUE_CLI_SERVICE`
- `VUE_CLI_MODERN_MODE`
- 任何与Vue CLI相关的变量

**Node版本设置:**
- 确保Node版本为18.x或更高（已在vercel.toml中配置）

### 步骤3：重新部署
1. 在Vercel控制台，进入 **Deployments** 标签页
2. 点击 **"Redeploy"** 按钮
3. 选择 **"Redeploy with existing Build Cache"**（如果可用）
4. 或者选择 **"Clear Cache and Redeploy"**（推荐）

### 步骤4：监控构建日志
部署过程中，密切关注构建日志，确保看到：
```
Running "npm run build"
> vite build
```

而不是：
```
> vue-cli-service build
```

## 故障排除

### 如果问题仍然存在

#### 方案A：手动指定构建命令
在Vercel项目设置中，显式设置构建命令：
```
cd env-setup/frontend && npm run build
```

#### 方案B：创建部署钩子
在项目根目录创建 `.vercel/project.json`：
```json
{
  "projectId": "your-project-id",
  "orgId": "your-org-id",
  "settings": {
    "framework": "vite",
    "buildCommand": "npm run build",
    "outputDirectory": "dist",
    "devCommand": "npm run dev",
    "installCommand": "npm ci --prefer-offline"
  }
}
```

#### 方案C：使用Vercel CLI本地部署
```bash
# 安装Vercel CLI
npm i -g vercel

# 登录
vercel login

# 部署（指定项目目录）
vercel --cwd env-setup/frontend
```

#### 方案D：解决vite命令未找到问题
如果出现 `vite: command not found` 错误：

1. **检查依赖安装**：
   ```bash
   # 在本地运行环境检查
   cd env-setup/frontend
   node scripts/check-environment.js
   ```

2. **确保package-lock.json存在**：
   ```bash
   # 如果package-lock.json不存在
   npm install --package-lock-only
   ```

3. **使用npm ci替代npm install**：
   ```bash
   # 在Vercel配置中使用
   npm ci --prefer-offline
   ```

4. **验证本地构建**：
   ```bash
   # 确保本地可以构建
   npm ci --prefer-offline
   npm run build
   ```

## 预防措施

### 1. 统一配置文件
确保所有Vercel配置文件一致：
- `vercel.json`: 包含 `"framework": "vite"`
- `vercel.toml`: 包含 `[framework] name = "vite"`

### 2. 明确package.json配置
在 `package.json` 中添加构建工具说明：
```json
{
  "name": "frontend",
  "private": true,
  "type": "module",
  "engines": {
    "node": ">=18.0.0"
  },
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

### 3. 添加构建验证脚本
使用我们创建的验证脚本：
```bash
cd env-setup/frontend
node scripts/verify-build.js
```

### 4. 文档记录
在README中明确说明技术栈：
```markdown
## 技术栈
- **前端框架**: Vue 3
- **构建工具**: Vite
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **部署平台**: Vercel
```

## 验证部署成功

部署成功后，检查以下内容：

### 1. 构建日志验证
✅ 应该显示：
```
Running "npm run build"
> vite build
vite v5.0.0 building for production...
✓ built in 1.23s
```

❌ 不应该显示：
```
> vue-cli-service build
```

### 2. 网站功能验证
访问部署的网站，检查：
- ✅ 页面正常加载
- ✅ 路由正常工作
- ✅ API连接正常（如果配置了后端）
- ✅ 静态资源正常加载

### 3. 性能检查
- ✅ 页面加载速度正常
- ✅ 资源文件正确缓存
- ✅ 没有控制台错误

## 联系支持

如果问题仍然无法解决：

1. **Vercel支持**:
   - 在Vercel控制台提交支持请求
   - 提供完整的构建日志
   - 提供项目配置截图

2. **社区支持**:
   - [Vercel社区论坛](https://vercel.com/community)
   - [GitHub Issues](https://github.com/vercel/vercel/issues)

## 总结

您的项目配置是正确的Vite项目配置。`vue-cli-service: command not found` 错误是由于Vercel错误识别构建工具导致的。按照本指南的步骤操作，应该能够成功部署。

**关键步骤**：
1. ✅ 清除Vercel构建缓存
2. ✅ 验证项目设置
3. ✅ 重新部署并监控日志
4. ✅ 验证部署成功

祝您部署顺利！