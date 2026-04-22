# 前端Vercel部署指南

## 1. 准备工作

### 1.1 获取后端API地址
1. 登录Railway控制台：https://railway.app
2. 找到你的校园二手交易平台后端项目
3. 复制部署域名（格式：`https://your-project-name.up.railway.app`）
4. 这个地址将作为`VITE_API_BASE_URL`环境变量的值

### 1.2 创建GitHub仓库（如果还没有）
```bash
cd /path/to/your/project
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/仓库名.git
git push -u origin main
```

## 2. Vercel部署步骤

### 2.1 登录Vercel
1. 访问 https://vercel.com
2. 使用GitHub账号登录
3. 点击"New Project"按钮

### 2.2 导入项目
1. 选择你的GitHub仓库
2. Vercel会自动检测到这是Vite项目
3. 点击"Import"按钮

### 2.3 配置项目设置
在项目设置页面，配置以下内容：

#### 项目名称
- 设置一个易记的项目名称，如：`campus-market-frontend`

#### 环境变量配置
点击"Environment Variables"选项卡，添加以下变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `VITE_API_BASE_URL` | `https://your-railway-project.up.railway.app` | **最重要**：替换为你的Railway后端地址 |
| `VITE_APP_NAME` | `校园二手交易平台` | 应用名称 |
| `VITE_APP_VERSION` | `1.0.0` | 应用版本 |
| `VITE_DEBUG` | `false` | 生产环境禁用调试 |
| `VITE_FRONTEND_URL` | `https://your-vercel-project.vercel.app` | 前端部署地址（部署后自动更新） |

#### 构建配置
- **Framework Preset**: Vite
- **Build Command**: `npm run build`（默认）
- **Output Directory**: `dist`（默认）
- **Install Command**: `npm install`（默认）

### 2.4 部署
1. 点击"Deploy"按钮
2. 等待构建完成（约1-3分钟）
3. 部署成功后，Vercel会提供一个预览URL，如：`https://your-project.vercel.app`

## 3. 验证部署

### 3.1 访问网站
1. 打开Vercel提供的预览URL
2. 检查页面是否正常加载

### 3.2 测试功能
1. **用户注册/登录**：测试用户认证功能
2. **商品浏览**：查看商品列表和详情
3. **发布商品**：测试商品发布功能
4. **消息系统**：测试消息发送和接收

### 3.3 检查控制台
1. 打开浏览器开发者工具（F12）
2. 检查Console标签页是否有错误
3. 检查Network标签页的API请求是否正常

## 4. 常见问题解决

### 4.1 CORS错误
如果出现CORS错误，需要检查：
1. 后端CORS配置是否正确
2. `VITE_API_BASE_URL`是否设置正确
3. 后端是否允许前端域名的请求

### 4.2 API连接失败
1. 检查Railway后端是否正常运行
2. 访问后端健康检查端点：`https://your-railway-project.up.railway.app/actuator/health`
3. 检查网络连接

### 4.3 构建失败
1. 检查Vercel构建日志
2. 确保`package.json`中的依赖正确
3. 检查Node.js版本兼容性

## 5. 后续维护

### 5.1 更新部署
每次向GitHub仓库推送代码时，Vercel会自动重新部署。

### 5.2 自定义域名
如果需要自定义域名：
1. 在Vercel项目设置中选择"Domains"
2. 添加你的自定义域名
3. 按照指引配置DNS记录

### 5.3 环境管理
Vercel支持多环境：
- **Production**: 生产环境
- **Preview**: 每次PR创建的预览环境
- **Development**: 开发环境

## 6. 联系支持

如果遇到问题：
1. 查看Vercel文档：https://vercel.com/docs
2. 检查Railway日志：https://railway.app
3. 查看项目GitHub Issues

---

**部署成功标志**：
- ✅ 网站可正常访问
- ✅ 用户可注册/登录
- ✅ 商品可浏览和发布
- ✅ 消息系统正常工作
- ✅ 无控制台错误

祝您部署顺利！