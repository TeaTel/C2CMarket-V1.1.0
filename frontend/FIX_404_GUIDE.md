# Vercel 404错误修复指南

## 问题描述
访问Vercel部署的网站时出现404错误，但部署状态显示成功。

## 根本原因
Vercel没有正确配置单页应用（SPA）路由。当用户访问非根路径（如`/login`、`/products`）时，Vercel尝试查找对应的静态文件，但找不到，因此返回404。

## 已实施的修复

### 1. 更新Vercel配置文件
#### `vercel.json` - 主要配置文件
```json
{
  "rewrites": [
    {
      "source": "/(.*)",
      "destination": "/"
    }
  ]
}
```
**作用**：将所有请求重写到根路径，让Vue Router处理路由。

#### `vercel.toml` - 备用配置文件
```toml
[[redirects]]
source = "/(.*)"
destination = "/"
status = 200
```
**作用**：使用Vercel官方TOML格式提供相同的路由配置。

### 2. 添加自定义404页面
创建了`public/404.html`文件：
- 提供友好的错误提示
- 自动重定向到首页
- 保持品牌一致性

### 3. 验证Vite配置
确保`vite.config.js`正确配置：
```javascript
base: process.env.NODE_ENV === 'production' ? '/' : '/'
```

## 部署步骤

### 步骤1：触发重新部署
由于代码已推送到GitHub，Vercel会自动重新部署。如果未自动触发：

1. 登录Vercel控制台：https://vercel.com
2. 找到你的项目
3. 点击"Redeploy"按钮

### 步骤2：验证部署
部署完成后：

1. **访问根路径**：`https://your-project.vercel.app`
   - 应该显示应用首页
   
2. **测试路由导航**：
   - 点击导航栏链接（如"登录"、"商品"）
   - 检查URL是否正确更新
   - 确认页面内容正常显示

3. **直接访问子路径**：
   - 直接访问`https://your-project.vercel.app/login`
   - 应该显示登录页面，而不是404

### 步骤3：检查环境变量
确保以下环境变量已正确设置：

| 变量名 | 值 | 检查方法 |
|--------|-----|----------|
| `VITE_API_BASE_URL` | 你的Railway后端地址 | 访问`{VITE_API_BASE_URL}/actuator/health` |
| `VITE_APP_NAME` | `校园二手交易平台` | 查看页面标题 |
| `VITE_DEBUG` | `false` | 控制台不应有调试日志 |

## 故障排除

### 如果仍然出现404

#### 方案1：清除Vercel缓存
1. 在Vercel项目设置中
2. 找到"Deployments"选项卡
3. 点击"Clear Build Cache"
4. 重新部署

#### 方案2：手动配置路由
在Vercel项目设置中：
1. 进入"Settings" → "Git"
2. 检查"Production Branch"是否为`main`
3. 进入"Settings" → "Build & Development Settings"
4. 确认以下配置：
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install`

#### 方案3：检查构建日志
1. 在Vercel控制台查看最新部署的构建日志
2. 查找错误或警告信息
3. 确保构建成功完成

### 常见错误模式

#### 错误1：空白页面
- **原因**：JavaScript加载失败
- **解决**：检查控制台错误，确认资源路径正确

#### 错误2：路由跳转后404
- **原因**：Vercel路由配置未生效
- **解决**：等待几分钟让配置生效，或重新部署

#### 错误3：API调用失败
- **原因**：`VITE_API_BASE_URL`配置错误
- **解决**：检查后端是否运行，CORS配置是否正确

## 验证清单

部署成功后，请验证以下功能：

### 基础功能
- [ ] 网站可正常访问（无404）
- [ ] 页面标题显示正确
- [ ] 导航栏工作正常
- [ ] 页面样式正确加载

### 路由功能
- [ ] 直接访问`/login`显示登录页面
- [ ] 直接访问`/products`显示商品页面
- [ ] 直接访问`/register`显示注册页面
- [ ] 浏览器前进/后退按钮工作正常

### API功能
- [ ] 用户可注册/登录
- [ ] 商品列表可加载
- [ ] 商品详情可查看
- [ ] 消息系统可工作

### 错误处理
- [ ] 访问不存在的路径显示自定义404页面
- [ ] 404页面5秒后自动重定向到首页
- [ ] 控制台无JavaScript错误

## 技术原理

### 单页应用路由原理
1. **Vue Router使用History模式**：URL看起来像普通路径（`/login`）
2. **Vercel需要特殊配置**：因为实际没有`/login.html`文件
3. **重写规则**：告诉Vercel将所有请求指向`index.html`
4. **Vue Router接管**：根据URL显示对应组件

### 为什么需要两个配置文件？
- `vercel.json`：JSON格式，易于阅读
- `vercel.toml`：TOML格式，Vercel官方推荐
- 双保险确保至少一个配置生效

## 后续维护

### 自动部署
- 每次推送到GitHub `main`分支，Vercel会自动部署
- 预览环境：每个Pull Request会创建临时部署

### 监控建议
1. **性能监控**：使用Vercel Analytics
2. **错误监控**：考虑集成Sentry
3. **用户分析**：添加Google Analytics

### 更新策略
1. **测试环境**：先部署到预览环境测试
2. **生产环境**：确认无误后合并到`main`分支
3. **回滚**：Vercel支持快速回滚到之前的部署

## 联系支持

如果问题仍未解决：
1. **查看Vercel文档**：https://vercel.com/docs
2. **检查GitHub Issues**：项目代码库的问题跟踪
3. **联系Vercel支持**：通过控制台提交支持请求

---

**修复完成标志**：
- ✅ 所有路由可正常访问
- ✅ 自定义404页面工作正常
- ✅ API调用成功
- ✅ 用户功能完整

现在你的校园二手交易平台应该可以正常工作了！